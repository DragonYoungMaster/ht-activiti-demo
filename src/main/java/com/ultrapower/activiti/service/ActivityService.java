package com.ultrapower.activiti.service;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
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

  private static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<>();

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private RepositoryService repositoryService;

  /**
   * 启动流程
   */
  public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
  }

  /**
   * 查询等待签收的任务
   */
  public List<Task> findTasksOfToClaim(String userId) {
    return taskService.createTaskQuery().taskCandidateUser(userId).active().list();
  }

  /**
   * 查询已经签收的任务
   */
  public List<Task> findTasksOfToDo(String userId) {
    return taskService.createTaskQuery().taskAssignee(userId).active().list();
  }

  public List<Task> findTasks(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskCandidateOrAssigned(userId).list()
        : taskQuery.taskCandidateOrAssigned(userId).list();
  }

  public List<Task> findTasksOfAssignee(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskAssignee(userId).list()
        : taskQuery.taskAssignee(userId).list();
  }

  public List<Task> findTasksOfCandidateUser(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskCandidateUser(userId).list()
        : taskQuery.taskCandidateUser(userId).list();
  }

  public void completeTask(String taskId, String userId, Map<String, Object> variables) {
    taskService.claim(taskId, userId);
    taskService.complete(taskId, variables);
  }

  /**
   * 设置流程变量
   */
  public void setTaskVariables(String taskId, Map<String, ? extends Object> variables) {
    taskService.setVariables(taskId, variables);
  }

  /**
   * 获取流程变量
   */
  public Map<String, Object> getTaskVariables(String taskId) {
    return taskService.getVariables(taskId);
  }

  /**
   * 查询流程定义
   */
  public ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return PROCESS_DEFINITION_CACHE.computeIfAbsent(processDefinitionId
        , i -> repositoryService.createProcessDefinitionQuery().processDefinitionId(i).singleResult());
  }
}

