package com.ultrapower;

import com.ultrapower.activiti.Application;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ActivitiDemoTests {

	@Test
	public void testProcess() {
		//获取流程引擎(会自动创建数据库表)
		ProcessEngine processEngine = getProcessEngine();

		//发布流程 部署流程定义
		Deployment deployment = deploy(processEngine);

		//查询流程定义
		List<ProcessDefinition> processDefinitions = getProcessDefinitions(processEngine);
		for(ProcessDefinition processDefinition : processDefinitions){
			System.out.println(processDefinition.getKey());
		}

		//启动流程
		ProcessInstance processInstance = startProcess(processEngine, "myProcess_1");

		//查询任务
		List<Task> tasks = queryTask(processEngine, "张三");
		for (Task task : tasks) {
			System.out.println(task);
		}
	}

	private ProcessEngine getProcessEngine() {
		return ProcessEngines.getDefaultProcessEngine();
	}

	private Deployment deploy(ProcessEngine processEngine) {
		return processEngine.getRepositoryService().createDeployment()
				.addClasspathResource("bpmn/process1.bpmn").addClasspathResource("bpmn/process1.png")
				.deploy();
	}

	private List<ProcessDefinition> getProcessDefinitions(ProcessEngine processEngine) {
		return processEngine.getRepositoryService().createProcessDefinitionQuery().list();
	}

	private ProcessInstance startProcess(ProcessEngine processEngine, String processDefinitionKey) {
		return processEngine.getRuntimeService().startProcessInstanceByKey(processDefinitionKey);
	}

	private List<Task> queryTask(ProcessEngine processEngine, String assignee) {
		return processEngine.getTaskService().createTaskQuery().taskAssignee(assignee).list();
	}




}
