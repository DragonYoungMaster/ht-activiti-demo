package com.ultrapower.activiti.service;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: teng.he
 * time: 11:38 2018/12/6
 */
@Service
public class ActivityService {

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  /**
   * 启动流程
   */
  public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
  }

  /**
   * 查询待办任务列表
   */
  public List<Task> findTasksOfAssignee(String userId) {
    return findTasksOfAssignee(null, userId);
  }

  /**
   * 查询待办任务列表
   */
  public List<Task> findTasksOfAssignee(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskAssignee(userId).list()
        : taskQuery.taskAssignee(userId).list();
  }

  /**
   * 查询待办任务列表
   */
  public List<Task> findTasksOfCandidateUser(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskCandidateUser(userId).list()
        : taskQuery.taskCandidateUser(userId).list();
  }

  public void completeTask(String taskId, String userId, String result) {
    //获取流程实例
    taskService.claim(taskId, userId);

    Map<String,Object> vars = new HashMap<>();
    vars.put("sign", result);

    taskService.complete(taskId, vars);
  }

  /**
   *
   * 任务审批 	（通过/拒接）
   * @param taskId 任务id
   * @param userId 用户id
   * @param result false OR true
   */
  public void completeTask(String taskId, String userId, Boolean result) {
    //获取流程实例
    taskService.claim(taskId, userId);

    Map<String,Object> vars = new HashMap<>();
    vars.put("deptLeaderPass", result ? "1" : "0");
    taskService.complete(taskId, vars);
  }
}

