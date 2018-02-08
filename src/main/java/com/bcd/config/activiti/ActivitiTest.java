package com.bcd.config.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivitiTest {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    private ActivitiRule activitiRule=new ActivitiRule();

    @Test
    public void testProcessTest(){
        ProcessInstance processInstance= runtimeService.startProcessInstanceByKey("testProcess");
    }
}
