package com.example.service;

import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EquipmentService {
    String addEquipment(String userId, Integer money);

    List<Map<String, Object>> list(String userId);

    String apply(String taskId);

    String reject(String taskId);

    void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws IOException;

    List<HistoricProcessInstance> historyList();

    List<HistoricActivityInstance> activities(String userId);

    List<HistoricProcessInstance> getdoed(String assignee);
}
