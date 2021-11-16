package com.bcd.sys.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Transient;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("unchecked")
public class TaskRunnable<T extends Task> implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TaskRunnable.class);

    private final T task;
    private Object[] params;
    private volatile boolean stop = false;
    private String functionName;

    /**
     * {@link #function}和{@link #executor}
     * 不支持序列化、需要通过{@link #init()}来初始化
     */
    @Transient
    private TaskFunction<T> function;
    @Transient
    private ThreadPoolExecutor executor;

    public TaskRunnable(T task, String functionName, Object[] params) {
        this.task = task;
        this.functionName = functionName;
        this.params = params;

    }

    public void init() {
        //初始化function
        function = TaskFunction.from(functionName);
        //分配线程
        executor = CommonConst.TASK_POOLS[task.getId().hashCode() % CommonConst.TASK_POOLS.length];
    }


    public T getTask() {
        return task;
    }

    public void stop() {
        boolean removed = executor.remove(this);
        if (removed) {
            //取消成功
            executor.execute(()->{
                TaskUtil.onCanceled(task);
            });
        } else {
            //如果失败、说明任务正在执行
            if (function.supportStop()) {
                //如果方法支持运行中打断、调用shutdown
                function.stop(this);
            }
        }
    }

    @Override
    public void run() {
        //触发开始方法
        TaskUtil.onStarted(task);
        try {
            //执行任务
            final boolean apply = function.execute(this);
            if (apply) {
                TaskUtil.onSucceed(task);
            } else {
                TaskUtil.onStopped(task);
            }
        } catch (Exception ex) {
            logger.error("execute task[" + task.getId() + "] failed", ex);
            TaskUtil.onFailed(task, ex);
        } finally {
            //最后从当前服务器任务id和结果映射结果集中移除
            CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.remove(task.getId().toString());
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

    public TaskFunction<T> getFunction() {
        return function;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
