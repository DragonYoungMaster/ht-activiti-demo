package com.ultrapower.activiti.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * author: teng.he
 * time: 11:19 2018/12/11
 * desc:
 */
public class BaseController {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected TaskService taskService;

  @Autowired
  protected RuntimeService runtimeService;
}
