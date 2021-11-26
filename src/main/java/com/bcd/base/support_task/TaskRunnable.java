package com.bcd.base.support_task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("unchecked")
public class TaskRunnable<T extends Task<K>, K extends Serializable> implements Runnable,Serializable{
    private final static Logger logger = LoggerFactory.getLogger(TaskRunnable.class);

    private final static long serialVersionUID = 1L;

    private T task;
    private Object[] params;
    private volatile boolean stop = false;
    private String functionName;
    private String taskBuilderName;

    /**
     * {@link #function}、{@link #executor}、{@link #taskBuilder}
     * 不支持序列化、需要通过{@link #init()}来初始化
     */
    private transient TaskFunction<T, K> function;
    private transient ThreadPoolExecutor executor;
    private transient TaskBuilder<T, K> taskBuilder;

    public TaskRunnable(T task, TaskFunction<T, K> function, Object[] params, TaskBuilder<T, K> taskBuilder) {
        this.task = task;
        this.function = function;
        this.functionName = function.getName();
        this.params = params;
        this.taskBuilder = taskBuilder;
        this.taskBuilderName = taskBuilder.name;
    }

    public void init() {
        if (function == null) {
            //初始化function
            function = TaskFunction.from(functionName);
        }
        if (taskBuilder == null) {
            //初始化taskBuilder
            taskBuilder = TaskBuilder.from(taskBuilderName);
        }
        //分配线程
        executor = taskBuilder.executorChooser.next();
    }


    public T getTask() {
        return task;
    }

    public StopResult stop() {
        boolean removed = executor.remove(this);
        if (removed) {
            //取消成功
            executor.execute(() -> {
                task = taskBuilder.onCanceled(task);
            });
            return StopResult.CANCEL_SUCCEED;
        } else {
            //如果失败、说明任务正在执行
            if (function.supportStop()) {
                stop = true;
                //如果方法支持运行中打断、调用shutdown
                function.stop(this);
                return StopResult.IN_EXECUTING_INTERRUPT_SUCCEED;
            }else{
                return StopResult.IN_EXECUTING_INTERRUPT_NOT_SUPPORT;
            }
        }
    }

    @Override
    public void run() {
        //触发开始方法
        task = taskBuilder.onStarted(task);
        try {
            //执行任务
            final boolean apply = function.execute(this);
            //执行完毕之后、task可能被更新、此时重新加载
            task = taskBuilder.getTaskDao().doRead(task.getId());
            if (apply) {
                task = taskBuilder.onSucceed(task);
            } else {
                task = taskBuilder.onStopped(task);
            }
        } catch (Exception ex) {
            logger.error("execute task[" + task.getId() + "] failed", ex);
            task = taskBuilder.onFailed(task, ex);
        } finally {
            //最后从当前服务器任务id和结果映射结果集中移除
            taskBuilder.taskIdToRunnable.remove(task.getId().toString());
        }
    }


    public Object[] getParams() {
        return params;
    }

    public boolean isStop() {
        return stop;
    }

    public String getFunctionName() {
        return functionName;
    }

    public TaskFunction<T, K> getFunction() {
        return function;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
