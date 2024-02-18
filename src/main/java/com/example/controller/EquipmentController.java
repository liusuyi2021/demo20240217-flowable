package com.example.controller;

import com.example.domain.UserTask;
import com.example.service.EquipmentService;
import com.example.utils.AjaxResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @ClassName: EquipmentController
 * @Author: 刘苏义
 * @Date: 2024年02月18日14:07:43
 **/
@Controller
@RequestMapping("/equipment")
public class EquipmentController {
    @Resource
    private EquipmentService equipmentService;

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("userTask", new UserTask());
        List<Map<String, Object>> list = equipmentService.list("111");
        model.addAttribute("userTasks", list);
        return "index";
    }

    @PostMapping("/add")
    public String addEquipment(@ModelAttribute("userTask") UserTask userTask, Model model) {
        if (userTask.getUserId() == null || userTask.getMoney() == null) {
            model.addAttribute("message", "参数不能为空");
        } else {
            String res = equipmentService.addEquipment(userTask.getUserId(), userTask.getMoney());
            model.addAttribute("message", res);
        }
        List<Map<String, Object>> list = equipmentService.list("111");
        model.addAttribute("userTasks", list);
        return "index";
    }

    @RequestMapping("/list")
    @ResponseBody
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> list = equipmentService.list("111");
        return list;
    }
}
