package com.example.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.utils.AjaxResult;
import com.example.utils.CommUtil;
import liquibase.pro.packaged.S;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mapper.Mapper;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "expense")
@Slf4j
public class ExpenseController {
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

    Map<String, Object>ProcessInstanceMap=new HashMap<>();

/***************此处为业务代码******************/
    /**
     * 添加报销
     *
     * @param userId    用户Id
     * @param money     报销金额
     * @param descption 描述
     */
    @RequestMapping(value = "add")
    @ResponseBody
    public AjaxResult addExpense(String userId, Integer money, String descption) {
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", userId);
        map.put("money", money);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Equipment", map);
        ProcessInstanceMap.put(userId, processInstance);
        return AjaxResult.success("提交成功.流程Id为：" + processInstance.getId());
    }
    /**
     * 获取审批管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public AjaxResult list(String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        Map<String,Object> map = new HashMap<>();
        List<Map<String, Object>> listMap = new ArrayList<>();
        String[] ps ={"id","name","assignee"};
        for (Task task : tasks) {
            ProcessInstanceMap.put(userId, task.getProcessInstanceId());
            System.out.println(task.toString());
            map.put(task.getId(),task.getName());
            Map<String, Object> obj2map = CommUtil.obj2map(task, ps);
            listMap.add(obj2map);
        }
        return AjaxResult.success(listMap);
    }
    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @RequestMapping(value = "apply")
    @ResponseBody
    public AjaxResult apply(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        return AjaxResult.success("processed ok!");
    }
    /**
     * 拒绝
     */
    @ResponseBody
    @RequestMapping(value = "reject")
    public AjaxResult reject(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        return AjaxResult.success("reject");
    }
    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
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

    /**
     * 查询历史任务
     */
    @ResponseBody
    @RequestMapping(value = "historyList")
    public AjaxResult historyList() {
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().list();
        for (HistoricProcessInstance hi : list) {
            log.info("==={},{},{},{},{},{}",hi.getId(),hi.getName(),hi.getStartActivityId(),hi.getStartTime(),hi.getEndActivityId(),hi.getEndTime());
        }
        return AjaxResult.success();
    }
    /**
     * 查询历史任务
     */
    @ResponseBody
    @RequestMapping(value = "activities")
    public AjaxResult activities (String userId) {
        if(!ProcessInstanceMap.containsKey(userId))
        {
            return AjaxResult.error("没有查询到流程");
        }
        String ProcessInstanceId = ProcessInstanceMap.get(userId).toString();
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(ProcessInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance hi : activities) {
            log.info(hi.getActivityName());
        }
        return AjaxResult.success(activities);
    }

    /**
     * @assignee 指定人id
     **/
    @ResponseBody
    @RequestMapping(value = "getdoed")
    public List<HistoricProcessInstance> getdoed(String assignee) {
        return historyService.createHistoricProcessInstanceQuery().involvedUser(assignee).orderByProcessInstanceStartTime().desc().list();
    }
}
