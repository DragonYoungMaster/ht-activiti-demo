package com.ultrapower.activiti.controller;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/workflow/processinstance")
public class ProcessInstanceController extends BaseController {

    @RequestMapping(value = "list")
    @ResponseBody
    public List<ProcessInstance> list() {
        return runtimeService.createProcessInstanceQuery().list();
    }
}
