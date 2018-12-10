package com.ultrapower;

import com.google.common.collect.Maps;

import com.ultrapower.activiti.Application;
import com.ultrapower.activiti.Domain.User;
import com.ultrapower.activiti.service.ActivityService;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	@Autowired
	private HistoryService historyService;


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

	//设置流程变量【实体】
	public void setTasksVarEntity() {
		List<Task> lists = activityService.findTasksOfAssignee(PROCESS_DEFINITION_KEY, APPLY_USER_ID);
		for(Task task : lists) {
			User user = new User();
			user.setName("翠花");
			user.setId(UUID.randomUUID().toString());
			user.setTime(new Date());
			user.setNote("回去探亲，一起吃个饭123");
			taskService.setVariable(task.getId(), "人员信息(添加固定版本)", user);

			System.out.println("设置流程变量成功！");
		}
	}

	//获取流程变量【实体】  实体必须序列化
	public void getTasksVarEntity() {
		List<Task> lists = activityService.findTasksOfAssignee(PROCESS_DEFINITION_KEY,"ht");
		for(Task task : lists) {
			// 2.获取流程变量，使用javaBean类型
			User user = (User)taskService.getVariable(task.getId(), "人员信息(添加固定版本)");
			System.out.println(" 请假人：  "+user.getName()+"  请假天数：  "+user.getId()+"   请假时间："+ user
					.getTime() + "   请假原因： "+user.getNote());
		}
	}


	//生成流程图
	public void queryProImg() throws Exception {
		activityService.queryProImg("232501");
	}

	//生成流程图（高亮）
	public void queryProHighLighted() throws Exception {
		activityService.queryProHighLighted("232501");
	}

	/**
	 * 查询流程变量的历史表,可以根据变量名称查询该变量的所有历史信息
	 */
	public void findHistoryProcessVariables() {
		List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()
				.variableName("请假天数").list();
		if (list != null && list.size() > 0) {
			for (HistoricVariableInstance hvi : list) {
				System.out.println(hvi.getId()+"     "+hvi.getProcessInstanceId()+"   "+hvi.getVariableName()
						+"   "+hvi.getVariableTypeName()+"    "+hvi.getValue());
				System.out.println("########################################");
			}
		}
	}

	/**
	 *  历史流程实例查询
	 */
	public void findHistoricProcessInstance() {
		// 查询已完成的流程
		List<HistoricProcessInstance> datas = historyService
				.createHistoricProcessInstanceQuery().finished().list();
		System.out.println("使用finished方法：" + datas.size());
		// 根据流程定义ID查询
		datas = historyService.createHistoricProcessInstanceQuery()
				.processDefinitionId("processDefinitionId").list();
		System.out.println("使用processDefinitionId方法： " + datas.size());
		// 根据流程定义key（流程描述文件的process节点id属性）查询
		datas = historyService.createHistoricProcessInstanceQuery()
				.processDefinitionKey("processDefinitionKey").list();
		System.out.println("使用processDefinitionKey方法： " + datas.size());
		// 根据业务主键查询
		datas = historyService.createHistoricProcessInstanceQuery()
				.processInstanceBusinessKey("processInstanceBusinessKey").list();
		System.out.println("使用processInstanceBusinessKey方法： " + datas.size());
		// 根据流程实例ID查询
		datas = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId("processInstanceId").list();
		System.out.println("使用processInstanceId方法： " + datas.size());
		// 查询没有完成的流程实例
		historyService.createHistoricProcessInstanceQuery().unfinished().list();
		System.out.println("使用unfinished方法： " + datas.size());
	}

	/**
	 *  历史任务查询
	 */
	public void findHistoricTasks() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//历史数据查询
		List<HistoricTaskInstance> datas = historyService.createHistoricTaskInstanceQuery()
				.finished().list();
		System.out.println("使用finished方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processDefinitionId("processDefinitionId").list();
		System.out.println("使用processDefinitionId方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processDefinitionKey("testProcess").list();
		System.out.println("使用processDefinitionKey方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processDefinitionName("testProcess2").list();
		System.out.println("使用processDefinitionName方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processFinished().list();
		System.out.println("使用processFinished方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId("processInstanceId").list();
		System.out.println("使用processInstanceId方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.processUnfinished().list();
		System.out.println("使用processUnfinished方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskAssignee("crazyit").list();
		System.out.println("使用taskAssignee方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskAssigneeLike("%zy%").list();
		System.out.println("使用taskAssigneeLike方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskDefinitionKey("usertask1").list();
		System.out.println("使用taskDefinitionKey方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskDueAfter(sdf.parse("2020-10-11 06:00:00")).list();
		System.out.println("使用taskDueAfter方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskDueBefore(sdf.parse("2022-10-11 06:00:00")).list();
		System.out.println("使用taskDueBefore方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.taskDueDate(sdf.parse("2020-10-11 06:00:00")).list();
		System.out.println("使用taskDueDate方法查询：" + datas.size());
		datas = historyService.createHistoricTaskInstanceQuery()
				.unfinished().list();
		System.out.println("使用unfinished方法查询：" + datas.size());
	}

	/**
	 *  历史行为查询
	 *  流程在进行过程中，每每走一个节点，都会记录流程节点的信息，包括节点的id，名称、类型、时间等，保存到ACT_HI_ACTINST表中。
	 */
	public void findHistoricActivityInstance() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//查询数据
		List<HistoricActivityInstance> datas = historyService.createHistoricActivityInstanceQuery()
				.activityId("endevent1").list();
		System.out.println("使用activityId查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery()
				.activityInstanceId(datas.get(0).getId()).list();
		System.out.println("使用activityInstanceId查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery()
				.activityType("intermediateSignalCatch").list();
		System.out.println("使用activityType查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery()
				.executionId("executionId").list();
		System.out.println("使用executionId查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery().finished().list();
		System.out.println("使用finished查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId("processInstanceId").list();
		System.out.println("使用processInstanceId查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery()
				.taskAssignee("crazyit").list();
		System.out.println("使用taskAssignee查询：" + datas.size());
		datas = historyService.createHistoricActivityInstanceQuery().unfinished().list();
		System.out.println("使用unfinished查询：" + datas.size());
	}

	/**
	 *  历史流程明细查询
	 *  在流程进行的过程中，会产生许多明细数据，只有将History设置为最高级别的时候，才会被记录到ACT_HI_DETAIL表中。
	 */
	public void findHistoricDetail() {
		// 查询历史行为
		HistoricActivityInstance act = historyService.createHistoricActivityInstanceQuery()
				.activityName("First Task").finished().singleResult();
		List<HistoricDetail> datas = historyService.createHistoricDetailQuery()
				.activityInstanceId(act.getId()).list();
		System.out.println("使用activityInstanceId方法查询：" + datas.size());
		datas = historyService.createHistoricDetailQuery().excludeTaskDetails().list();
		System.out.println("使用excludeTaskDetails方法查询：" + datas.size());
		datas = historyService.createHistoricDetailQuery().formProperties().list();
		System.out.println("使用formProperties方法查询：" + datas.size());
		datas = historyService.createHistoricDetailQuery().processInstanceId("processInstanceId").list();
		System.out.println("使用processInstanceId方法查询：" + datas.size());
		datas = historyService.createHistoricDetailQuery().taskId("taskId").list();
		System.out.println("使用taskId方法查询：" + datas.size());
		datas = historyService.createHistoricDetailQuery().variableUpdates().list();
		System.out.println("使用variableUpdates方法查询：" + datas.size());
	}

}
