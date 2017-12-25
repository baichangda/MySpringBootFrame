package com.bcd.config.plugins.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ServiceTask1 implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.err.println("Execute ServiceTask1 execute!");
        System.err.println(execution.getCurrentActivityId());
    }
}
