package com.ultrapower.activiti.controller;

import com.ultrapower.activiti.util.UserUtil;

import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * author: teng.he
 * time: 11:23 2018/12/11
 */
@Controller
@RequestMapping(value = "/workflow/task")
public class TaskController extends BaseController {

  private static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<String, ProcessDefinition>();

  /**
   * 待办任务
   */
  @RequestMapping(value = "/task/todo/list")
  @ResponseBody
  public List<Map<String, Object>> todoList(HttpSession session) throws Exception {
    User user = UserUtil.getUserFromSession(session);
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    // 已经签收的任务
    List<Task> todoList = taskService.createTaskQuery().taskAssignee(user.getId()).active().list();
    for (Task task : todoList) {
      String processDefinitionId = task.getProcessDefinitionId();
      ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);

      Map<String, Object> singleTask = packageTaskInfo(sdf, task, processDefinition);
      singleTask.put("status", "todo");
      result.add(singleTask);
    }

    // 等待签收的任务
    List<Task> toClaimList = taskService.createTaskQuery().taskCandidateUser(user.getId()).active().list();
    for (Task task : toClaimList) {
      String processDefinitionId = task.getProcessDefinitionId();
      ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);

      Map<String, Object> singleTask = packageTaskInfo(sdf, task, processDefinition);
      singleTask.put("status", "claim");
      result.add(singleTask);
    }

    return result;
  }

  private Map<String, Object> packageTaskInfo(SimpleDateFormat sdf, Task task, ProcessDefinition processDefinition) {
    Map<String, Object> singleTask = new HashMap<String, Object>();
    singleTask.put("id", task.getId());
    singleTask.put("name", task.getName());
    singleTask.put("createTime", sdf.format(task.getCreateTime()));
    singleTask.put("pdname", processDefinition.getName());
    singleTask.put("pdversion", processDefinition.getVersion());
    singleTask.put("pid", task.getProcessInstanceId());
    return singleTask;
  }

  private ProcessDefinition getProcessDefinition(String processDefinitionId) {
    ProcessDefinition processDefinition = PROCESS_DEFINITION_CACHE.get(processDefinitionId);
    if (processDefinition == null) {
      processDefinition = repositoryService.createProcessDefinitionQuery()
          .processDefinitionId(processDefinitionId).singleResult();
      PROCESS_DEFINITION_CACHE.put(processDefinitionId, processDefinition);
    }
    return processDefinition;
  }
}
