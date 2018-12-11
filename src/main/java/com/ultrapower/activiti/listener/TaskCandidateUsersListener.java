package com.ultrapower.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * author: teng.he
 * time: 22:07 2018/12/8
 */
@Service
public class TaskCandidateUsersListener implements TaskListener {
  private static final long serialVersionUID = 2265918881748770828L;

  @Override
  public void notify(DelegateTask delegateTask) {
    //查询当前用户
    String currentUserId = "ht";
    System.out.println("当前登录人========"+currentUserId);
    //查询当前用户领导
    String[] leaders = {"leader", "leader2", "leader3"};
    delegateTask.addCandidateUsers(Arrays.asList(leaders));
    System.out.println("节点任务人========leader, leader2, leader3");
  }
}
