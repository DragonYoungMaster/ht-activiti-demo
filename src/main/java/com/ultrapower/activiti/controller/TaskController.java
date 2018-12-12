package com.ultrapower.activiti.controller;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: teng.he
 * time: 11:23 2018/12/11
 */
@Controller
@RequestMapping(value = "/workflow/task")
public class TaskController extends BaseController {

  /**
   * 待办任务
   */
  @RequestMapping(value = "/todo/list")
  @ResponseBody
  public List<Map<String, Object>> todoList(@RequestParam("userId") String userId) throws Exception {
    List<Map<String, Object>> result = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    // 已经签收的任务
    List<Task> todoList = activityService.findTasksOfToDo(userId);
    for (Task task : todoList) {
      ProcessDefinition processDefinition = activityService.getProcessDefinition(task.getProcessDefinitionId());
      result.add(packageTaskInfo(sdf, task, processDefinition, "todo"));
    }

    // 等待签收的任务
    List<Task> toClaimList = activityService.findTasksOfToClaim(userId);
    for (Task task : toClaimList) {
      ProcessDefinition processDefinition = activityService.getProcessDefinition(task.getProcessDefinitionId());
      result.add(packageTaskInfo(sdf, task, processDefinition, "claim"));
    }

    return result;
  }

  private Map<String, Object> packageTaskInfo(SimpleDateFormat sdf, Task task
      , ProcessDefinition processDefinition, String status) {
    Map<String, Object> singleTask = new HashMap<>();
    singleTask.put("id", task.getId());
    singleTask.put("name", task.getName());
    singleTask.put("createTime", sdf.format(task.getCreateTime()));
    singleTask.put("processDefinitionName", processDefinition.getName());
    singleTask.put("processDefinitionVersion", processDefinition.getVersion());
    singleTask.put("processInstanceId", task.getProcessInstanceId());
    singleTask.put("status", status);
    return singleTask;
  }
}
