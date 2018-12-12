package com.ultrapower;

import com.google.common.collect.Maps;

import com.ultrapower.activiti.Application;
import com.ultrapower.activiti.service.ActivityService;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ActivitiTest {
	private static final String PROCESS_DEFINITION_KEY = "process";
	private static final String APPLY_USER_ID = "ht";
	private static final String DEPT_LEADER = "leader";

	@Autowired
	private ActivityService activityService;

	@Test
	public void testProcess() {
		//启动流程
		startProcess(PROCESS_DEFINITION_KEY, APPLY_USER_ID);

    //申请人提交请假申请单
		apply(PROCESS_DEFINITION_KEY, APPLY_USER_ID, createApplyInfo(APPLY_USER_ID, 3));

		//部门领导审批，拒绝
    audit(PROCESS_DEFINITION_KEY, DEPT_LEADER, createAuditInfo(false));

    //申请人重新调整请假申请单
    adjust(PROCESS_DEFINITION_KEY, APPLY_USER_ID, null);

    //申请人提交请假申请单
    apply(PROCESS_DEFINITION_KEY, APPLY_USER_ID, createApplyInfo(APPLY_USER_ID, 2));

    //部门领导审批，同意
    audit(PROCESS_DEFINITION_KEY, DEPT_LEADER, createAuditInfo(true));
	}

	private void apply(String processDefinitionKey, String applyUserID, Map<String, Object> variables) {
    findAndCompleteTask(processDefinitionKey, applyUserID, variables);
  }

  private void audit(String processDefinitionKey, String leaderId, Map<String, Object> variables) {
    findAndCompleteTask(processDefinitionKey, leaderId, variables);
  }

  private void adjust(String processDefinitionKey, String leaderId, Map<String, Object> variables) {
    findAndCompleteTask(processDefinitionKey, leaderId, variables);
  }

  private void findAndCompleteTask(String processDefinitionKey, String userId, Map<String, Object> variables) {
    List<Task> tasks = activityService.findTasks(processDefinitionKey, userId);
    String taskId = tasks.get(0).getId();
    activityService.completeTask(taskId, userId, variables);
  }

	private Map<String, Object> createApplyInfo(String userId, int days) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("请假人", userId);
		variables.put("请假日期", new Date());
		variables.put("请假天数", days);
		return variables;
	}

	private Map<String, Object> createAuditInfo(boolean pass) {
    Map<String,Object> variables = new HashMap<>();
    variables.put("deptLeaderPass", pass ? "1" : "0");
    return variables;
  }

	//启动流程
	public void startProcess(String processDefinitionKey, String applyUserId) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("applyUserId", applyUserId);
		ProcessInstance processInstance = activityService.startProcess(processDefinitionKey, variables);
		System.out.println("启动流程，id: " + processInstance.getProcessInstanceId());
	}
}
