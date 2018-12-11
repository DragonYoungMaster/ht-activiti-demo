package com.ultrapower.activiti.controller;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/workflow/processinstance")
public class ProcessInstanceController extends BaseController {

    @RequestMapping(value = "running")
    @ResponseBody
    public List<ProcessInstance> running() {
        return runtimeService.createProcessInstanceQuery().list();
    }
}
