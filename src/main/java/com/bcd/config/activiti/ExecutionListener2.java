package com.bcd.config.activiti;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionListener2 implements ExecutionListener,JavaDelegate{
    @Autowired
    private TaskService taskService;
    @Override
    public void notify(DelegateExecution execution) {
        System.err.println("Execute ExecutionListener2 notify!");
        taskService.complete(execution.getId());
    }

    @Override
    public void execute(DelegateExecution execution) {
        System.err.println("Execute ExecutionListener2 execution!");
    }
}
