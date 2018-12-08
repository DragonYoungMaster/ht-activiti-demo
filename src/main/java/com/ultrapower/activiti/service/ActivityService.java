package com.ultrapower.activiti.service;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

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

  @Autowired
  private HistoryService historyService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  /**
   * 启动流程
   */
  public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
  }

  /**
   * 查询待办任务列表
   */
  public List<Task> findTasks(String userId) {
    return findTasks(null, userId);
  }

  /**
   * 查询待办任务列表
   */
  public List<Task> findTasks(String processDefinitionKey, String userId) {
    TaskQuery taskQuery = taskService.createTaskQuery();
    return StringUtils.isNotBlank(processDefinitionKey)
        ? taskQuery.processDefinitionKey(processDefinitionKey).taskAssignee(userId).list()
        : taskQuery.taskAssignee(userId).list();
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

  /**
   *
   * 生成流程图
   * 首先启动流程，获取processInstanceId，替换即可生成
   */
  public void queryProImg(String processInstanceId) throws Exception {
    //获取历史流程实例
    HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    //根据流程定义获取输入流
    InputStream is = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
    BufferedImage bi = ImageIO.read(is);
    File file = new File("demo2.png");
    if(!file.exists()) file.createNewFile();
    FileOutputStream fos = new FileOutputStream(file);
    ImageIO.write(bi, "png", fos);
    fos.close();
    is.close();
    System.out.println("图片生成成功");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("userId").list();
    for(Task t : tasks) {
      System.out.println(t.getName());
    }
  }


  /**
   * 流程图高亮显示
   * 首先启动流程，获取processInstanceId，替换即可生成
   */
  public void queryProHighLighted(String processInstanceId) throws Exception {
    //获取历史流程实例
    HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();
    //获取流程图
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

    ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
    ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition
        (processInstance.getProcessDefinitionId());

    List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId).list();
    //高亮环节id集合
    List<String> highLightedActivitis = new ArrayList<>();

    //高亮线路id集合
    List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);

    for(HistoricActivityInstance tempActivity : highLightedActivitList){
      String activityId = tempActivity.getActivityId();
      highLightedActivitis.add(activityId);
    }
    //配置字体
    InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png"
        , highLightedActivitis, highLightedFlows,"宋体","微软雅黑","黑体",null,2.0);
    BufferedImage bi = ImageIO.read(imageStream);
    File file = new File("demo2.png");
    if(!file.exists()) file.createNewFile();
    FileOutputStream fos = new FileOutputStream(file);
    ImageIO.write(bi, "png", fos);
    fos.close();
    imageStream.close();
    System.out.println("图片生成成功");
  }

  /**
   * 获取需要高亮的线
   */
  private List<String> getHighLightedFlows(
      ProcessDefinitionEntity processDefinitionEntity,
      List<HistoricActivityInstance> historicActivityInstances) {

    List<String> highFlows = new ArrayList<>();// 用以保存高亮的线flowId
    for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
      ActivityImpl activityImpl = processDefinitionEntity
          .findActivity(historicActivityInstances.get(i)
              .getActivityId());// 得到节点定义的详细信息
      List<ActivityImpl> sameStartTimeNodes = new ArrayList<>();// 用以保存后需开始时间相同的节点
      ActivityImpl sameActivityImpl1 = processDefinitionEntity
          .findActivity(historicActivityInstances.get(i + 1)
              .getActivityId());
      // 将后面第一个节点放在时间相同节点的集合里
      sameStartTimeNodes.add(sameActivityImpl1);
      for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
        HistoricActivityInstance activityImpl1 = historicActivityInstances
            .get(j);// 后续第一个节点
        HistoricActivityInstance activityImpl2 = historicActivityInstances
            .get(j + 1);// 后续第二个节点
        if (activityImpl1.getStartTime().equals(
            activityImpl2.getStartTime())) {
          // 如果第一个节点和第二个节点开始时间相同保存
          ActivityImpl sameActivityImpl2 = processDefinitionEntity
              .findActivity(activityImpl2.getActivityId());
          sameStartTimeNodes.add(sameActivityImpl2);
        } else {
          // 有不相同跳出循环
          break;
        }
      }
      List<PvmTransition> pvmTransitions = activityImpl
          .getOutgoingTransitions();// 取出节点的所有出去的线
      for (PvmTransition pvmTransition : pvmTransitions) {
        // 对所有的线进行遍历
        ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
            .getDestination();
        // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
        if (sameStartTimeNodes.contains(pvmActivityImpl)) {
          highFlows.add(pvmTransition.getId());
        }
      }
    }
    return highFlows;
  }
}

