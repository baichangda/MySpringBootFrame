package com.bcd.config.plugins.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;

public class ExecutionListener1 implements ExecutionListener,JavaDelegate{
    @Override
    public void notify(DelegateExecution execution) {
        System.err.println("Execute ExecutionListener1 notify!");
    }

    @Override
    public void execute(DelegateExecution execution) {
        System.err.println("Execute ExecutionListener1 execution!");
    }
}
