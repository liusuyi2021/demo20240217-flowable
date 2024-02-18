package com.example.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.service.EquipmentService;
import com.example.utils.AjaxResult;
import com.example.utils.CommUtil;
import liquibase.pro.packaged.S;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mapper.Mapper;
import org.flowable.bpmn.model.Activity;
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

import javax.annotation.Resource;
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
    @Resource
    private EquipmentService equipmentService;


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
        String res = equipmentService.addEquipment(userId, money);
        return AjaxResult.success(res);
    }

    /**
     * 获取审批管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public AjaxResult list(String userId) {
        List<Map<String, Object>> listMap = equipmentService.list(userId);
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
        String apply = equipmentService.apply(taskId);
        return AjaxResult.success(apply);
    }

    /**
     * 拒绝
     */
    @ResponseBody
    @RequestMapping(value = "reject")
    public AjaxResult reject(String taskId) {
        String reject = equipmentService.reject(taskId);
        return AjaxResult.success(reject);
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        equipmentService.genProcessDiagram(httpServletResponse, processId);
    }

    /**
     * 查询历史任务
     */
    @ResponseBody
    @RequestMapping(value = "historyList")
    public AjaxResult historyList() {
        List<HistoricProcessInstance> historicProcessInstances = equipmentService.historyList();
        return AjaxResult.success(historicProcessInstances);
    }

    /**
     * 查询历史任务
     */
    @ResponseBody
    @RequestMapping(value = "activities")
    public AjaxResult activities(String userId) {
        List<HistoricActivityInstance> activities = equipmentService.activities(userId);
        return AjaxResult.success(activities);
    }

    /**
     * @assignee 指定人id
     **/
    @ResponseBody
    @RequestMapping(value = "getdoed")
    public List<HistoricProcessInstance> getdoed(String assignee) {
       return equipmentService.getdoed(assignee);
    }
}
