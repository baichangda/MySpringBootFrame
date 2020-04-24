package com.bcd.sys.task.cluster;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.NamedTaskFunction;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskContext;
import com.bcd.sys.task.TaskFunction;
import com.bcd.sys.task.single.SingleTaskContext;


public class ClusterTaskContext<T extends Task> extends TaskContext<T> {

    protected String functionName;

    public ClusterTaskContext(T task, NamedTaskFunction<T> function) {
        super(task);
        this.functionName = function.getName();
    }

    public ClusterTaskContext(T task,NamedTaskFunction<T> function, Object[] params) {
        super(task, params);
        this.functionName = function.getName();
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public TaskFunction<T> getFunction() {
        TaskFunction<T> function= NamedTaskFunction.from(functionName);
        if(function==null){
            throw BaseRuntimeException.getException("can't get function["+functionName+"]");
        }
        return function;
    }

    @Override
    public boolean equals(Object targetContext) {
        if(this.getClass()!=targetContext.getClass()){
            return false;
        }
        ClusterTaskContext<T> target= ((ClusterTaskContext<T>)targetContext);
        if(functionName.equals(target.getFunctionName())){
            return false;
        }
        if(!task.equals(target.getTask())){
            return false;
        }
        if(params.length!=target.params.length){
            return false;
        }
        Object[] targetParams= target.getParams();
        for(int i=0,end=params.length;i<end;i++){
            if(params[i]!=targetParams[i]){
                if(params[i]==null||targetParams[i]==null){
                    return false;
                }else{
                    if(!params[i].equals(targetParams[i])){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
