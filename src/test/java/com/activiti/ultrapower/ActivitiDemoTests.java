package com.activiti.ultrapower;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ActivitiDemoTests {

	//获取流程引擎(会自动创建数据库表)
	@Test
	public void testCreateProcessEngine() {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		System.out.println(processEngine);
	}

	//发布流程
	@Test
	public void testDeployProcess() {

	}

	//启动流程

	@Test
	public void testStartProcess() {

	}

}
