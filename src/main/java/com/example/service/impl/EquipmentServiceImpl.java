package com.example.service.impl;

import com.example.service.EquipmentService;
import com.example.utils.AjaxResult;
import com.example.utils.CommUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @ClassName: EquipmentServiceImpl
 * @Author: 刘苏义
 * @Date: 2024年02月18日14:05:34
 **/
@Service
@Slf4j
public class EquipmentServiceImpl implements EquipmentService {
    Map<String, Object> ProcessInstanceMap = new HashMap<>();
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    HistoryService historyService;

    @Override
    public String addEquipment(String userId, Integer money) {
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", userId);
        map.put("money", money);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Equipment", map);
        ProcessInstanceMap.put(userId, processInstance);
        return "提交成功.流程Id为：" + processInstance.getId();
    }

    @Override
    public List<Map<String, Object>> list(String userId) {
        List<Task> tasks = taskService.createTaskQuery().orderByTaskCreateTime().desc().list();
        List<Map<String, Object>> listMap = new ArrayList<>();
        String[] ps = {"id", "name", "assignee"};
        for (Task task : tasks) {
            ProcessInstanceMap.put(userId, task.getProcessInstanceId());
            System.out.println(task.toString());
            Map<String, Object> obj2map = CommUtil.obj2map(task, ps);
            listMap.add(obj2map);
        }
        return listMap;
    }

    @Override
    public String apply(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return "流程不存在";
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        //如果是同意，还需要继续走一步
        Task t = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        taskService.complete(t.getId());
        return "processed ok!";
    }

    @Override
    public String reject(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return "流程不存在";
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        //如果是同意，还需要继续走一步
        Task t = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        taskService.complete(t.getId());
        return "reject";
    }

    @Override
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws IOException {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public List<HistoricProcessInstance> historyList() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().list();
        for (HistoricProcessInstance hi : list) {
            log.info("==={},{},{},{},{},{}", hi.getId(), hi.getName(), hi.getStartActivityId(), hi.getStartTime(), hi.getEndActivityId(), hi.getEndTime());
        }
        return list;
    }

    @Override
    public List<HistoricActivityInstance> activities(String userId) {

        String ProcessInstanceId = ProcessInstanceMap.get(userId).toString();
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(ProcessInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance hi : activities) {
            log.info(hi.getActivityName());
        }
        return activities;
    }

    @Override
    public List<HistoricProcessInstance> getdoed(String assignee) {
        return historyService.createHistoricProcessInstanceQuery().involvedUser(assignee).orderByProcessInstanceStartTime().desc().list();
    }
}
