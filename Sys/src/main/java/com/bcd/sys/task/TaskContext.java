package com.bcd.sys.task;

import com.bcd.sys.task.cluster.ClusterTaskContext;
import com.bcd.sys.task.single.SingleTaskContext;

import java.io.Serializable;

public abstract class TaskContext<T extends Task> implements Serializable {
    protected T task;

    protected Object[] params;

    protected volatile boolean stop=false;

    public TaskContext(T task) {
        this(task,null);
    }

    public TaskContext(T task, Object[] params) {
        this.task = task;
        this.params = params;
    }

    public T getTask() {
        return task;
    }

    public Object[] getParams() {
        return params;
    }

    public abstract TaskFunction<T> getFunction();

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public final static <T extends Task>TaskContext<T> newTaskContext(T task,TaskFunction<T> function,Serializable... params){
        return new SingleTaskContext<>(task,function,params);
    }

    public final static <T extends Task>ClusterTaskContext<T> newClusterTaskContext(T task,NamedTaskFunction<T> function,Serializable ... params){
        return new ClusterTaskContext<>(task,function,params);
    }


}
