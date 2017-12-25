package com.bcd.config.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;

public class TaskListener2 implements TaskListener,JavaDelegate {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.err.println("Execute TaskListener2 notify!");
    }

    @Override
    public void execute(DelegateExecution execution) {
        System.err.println("Execute TaskListener2 execute!");
    }
}
