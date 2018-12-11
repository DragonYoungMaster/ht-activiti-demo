package com.ultrapower;

import com.google.common.collect.Maps;

import com.ultrapower.activiti.Application;
import com.ultrapower.activiti.service.ActivityService;

import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ActivitiTest {
	private static final String PROCESS_DEFINITION_KEY = "process";
	private static final String APPLY_USER_ID = "ht";
	private static final String DEPT_LEADER = "leader";

	@Resource
	private ActivityService activityService;

	@Autowired
	private TaskService taskService;

	@Test
	public void testProcess() {
		//启动流程
		startProcess(PROCESS_DEFINITION_KEY, APPLY_USER_ID);

		//申请人提请假申请单
		List<Task> approveTasks = findTasksOfAssignee(PROCESS_DEFINITION_KEY, APPLY_USER_ID);
		String approveTaskId = approveTasks.get(0).getId();
		setTasksVar(approveTaskId, createApproveInfo(APPLY_USER_ID, 3));
		completeTasks(approveTaskId, APPLY_USER_ID, "提交审核");

		//部门领导审批
		//List<Task> tasks = findTasksOfAssignee(PROCESS_DEFINITION_KEY, DEPT_LEADER);
		List<Task> tasks = findTasksOfCandidateUser(PROCESS_DEFINITION_KEY, DEPT_LEADER);

		String taskId = tasks.get(0).getId();
    Map<String, Object> vars = getTasksVar(taskId);
		//activityService.completeTask(taskId, DEPT_LEADER, true);


		activityService.completeTask(taskId, DEPT_LEADER, false);
    repeat();


	}

	//重新提交审批
	private void repeat() {
    //申请人重新调整请假申请单
    List<Task> approveTasks = findTasksOfAssignee(PROCESS_DEFINITION_KEY, APPLY_USER_ID);
    String approveTaskId = approveTasks.get(0).getId();
    completeTasks(approveTaskId, APPLY_USER_ID, "重新调整");

    //申请人提请假申请单
    approveTasks = findTasksOfAssignee(PROCESS_DEFINITION_KEY, APPLY_USER_ID);
    approveTaskId = approveTasks.get(0).getId();
    setTasksVar(approveTaskId, createApproveInfo(APPLY_USER_ID, 2));
    completeTasks(approveTaskId, APPLY_USER_ID, "提交审核");

    //部门领导审批
    //List<Task> tasks = findTasksOfAssignee(PROCESS_DEFINITION_KEY, DEPT_LEADER);
    List<Task> tasks = findTasksOfCandidateUser(PROCESS_DEFINITION_KEY, DEPT_LEADER);
    String taskId = tasks.get(0).getId();
    Map<String, Object> vars = getTasksVar(taskId);
    activityService.completeTask(taskId, DEPT_LEADER, true);
  }

	private Map<String, Object> createApproveInfo(String userId, int days) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("请假人", userId);
		variables.put("请假日期", new Date());
		variables.put("请假天数", days);
		return variables;
	}

	//启动流程
	public void startProcess(String processDefinitionKey, String applyUserId) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("applyUserId", applyUserId);
		ProcessInstance processInstance = activityService.startProcess(processDefinitionKey, variables);
		System.out.println("启动流程，id: " + processInstance.getProcessInstanceId());
	}

	//获取任务列表
	public List<Task> findTasksOfAssignee(String processDefinitionKey, String userId) {
		List<Task> lists = activityService.findTasksOfAssignee(processDefinitionKey, userId);
		System.out.println("任务列表："+lists);
		return lists;
	}

	//获取任务列表
	public List<Task> findTasksOfCandidateUser(String processDefinitionKey, String userId) {
		List<Task> lists = activityService.findTasksOfCandidateUser(processDefinitionKey, userId);
		System.out.println("任务列表："+lists);
		return lists;
	}

	public void completeTasks(String taskId, String userId, String result) {
		activityService.completeTask(taskId, userId, result);
	}

	//设置流程变量
	public void setTasksVar(String taskId, Map<String, ? extends Object> variables) {
		taskService.setVariables(taskId, variables);
	}

	//获取流程变量
	public Map<String, Object> getTasksVar(String taskId) {
    return taskService.getVariables(taskId);
	}

}
