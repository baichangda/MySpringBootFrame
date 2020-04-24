package com.bcd.sys.task.single;

import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskContext;
import com.bcd.sys.task.TaskFunction;


@SuppressWarnings("unchecked")
public class SingleTaskContext<T extends Task> extends TaskContext<T> {
    private TaskFunction<T> function;

    public SingleTaskContext(T task, TaskFunction<T> function) {
        super(task);
        this.function = function;
    }

    public SingleTaskContext(T task, TaskFunction<T> function, Object[] params) {
        super(task, params);
        this.function = function;
    }

    @Override
    public TaskFunction<T> getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object targetContext) {
        if(this.getClass()!=targetContext.getClass()){
            return false;
        }
        SingleTaskContext<T> target= ((SingleTaskContext<T>)targetContext);
        if(function!=target.getFunction()){
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
