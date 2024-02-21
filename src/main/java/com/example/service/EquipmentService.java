package com.example.service;

import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EquipmentService {

    //部署流程
    String createDeploy(String deployName);
    //删除流程
    void deleteDeploy(String deployId);
    //查询流程列表
    List<Map<String, Object>> deployList();

    String addEquipment(String userId, Integer money);

    List<Map<String, Object>> list(String userId);

    String apply(String taskId);

    String reject(String taskId);

    void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws IOException;

    List<HistoricProcessInstance> historyList();

    List<HistoricActivityInstance> activities(String userId);

    List<HistoricProcessInstance> getdoed(String assignee);
}
