package com.bcd.base.support_task;


import com.bcd.base.exception.BaseRuntimeException;

import java.io.Serializable;
import java.util.HashMap;

public abstract class TaskFunction<T extends Task<K>, K extends Serializable> {
    private final static HashMap<String, TaskFunction> storage = new HashMap<>();

    public static <T extends Task<K>, K extends Serializable> TaskFunction<T, K> from(String name) {
        return storage.get(name);
    }

    private final String name;

    public String getName() {
        return name;
    }

    public TaskFunction(String name) {
        this.name = name;
        synchronized (storage) {
            if (storage.containsKey(name)) {
                throw BaseRuntimeException.getException("TaskFunction[{}] [{}] exist", name, storage.get(name));
            } else {
                storage.put(name, this);
            }
        }
    }

    public TaskFunction() {
        this.name = this.getClass().getName();
        storage.put(this.name, this);
    }

    /**
     * task执行任务内容
     *
     * @param runnable 上下文环境
     * @return true: 执行成功、false: 任务被打断
     */
    public abstract boolean execute(TaskRunnable<T, K> runnable);

    /**
     * 方法是否支持打断操作
     * 如果为true、则{@link #stop(TaskRunnable)}会被调用
     *
     * @return
     */
    public boolean supportStop() {
        return false;
    }

    /**
     * 当用户希望打断任务、且任务{@link #supportStop()}为true时候
     * 此方法会被调用
     * 此方法会和{@link #execute(TaskRunnable)}同时执行、为两个不同的线程、需要注意线程安全问题
     * 主要作用是可以通过中间变量来通知{@link #execute(TaskRunnable)}完成打断操作
     * <p>
     * 需要注意的是:
     * 被打断后的{@link #execute(TaskRunnable)}返回必须是false
     *
     * @param runnable
     */
    public void stop(TaskRunnable<T, K> runnable) {

    }
}
