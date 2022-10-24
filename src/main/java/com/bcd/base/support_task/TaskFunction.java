package com.bcd.base.support_task;


import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class TaskFunction<T extends Task<K>, K extends Serializable> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public abstract void execute(TaskRunnable<T, K> runnable);

    /**
     * 方法是否支持打断操作
     *
     * @return
     */
    public boolean supportStop() {
        return false;
    }

    /**
     * 启动一个线程池、周期更新任务信息
     * <p>
     * 注意、需要手动关闭线程池
     *
     * @param runnable
     * @param period
     * @param doBeforeUpdate
     * @return
     */
    public ScheduledExecutorService updateTaskPeriodInNewThread(TaskRunnable<T, K> runnable, Duration period, Consumer<T> doBeforeUpdate) {
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            final T task = runnable.getTask();
            doBeforeUpdate.accept(task);
            runnable.getTaskDao().doUpdate(task);
        }, period.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
        return scheduledExecutorService;
    }


    private final ThreadLocal<Long> prevSaveTsThreadLocal = new ThreadLocal<>();
    /**
     * 在循环中使用更新任务、保证更新的周期不小于 period 参数
     * 主要是使用在for循环中、避免过快的更新
     * <p>
     * 备注:
     * 需要显式的在for循环中需要保存的地方调用
     *
     * @param runnable
     * @param period
     * @param doBeforeUpdate
     * @return 是否保存
     */
    protected boolean updateTaskPeriodInCurrentThread(TaskRunnable<T, K> runnable, Duration period, Consumer<T> doBeforeUpdate) {
        final Long prevSaveTs = prevSaveTsThreadLocal.get();
        final long curTs = System.currentTimeMillis();
        if (prevSaveTs == null || (curTs - prevSaveTs) > period.toMillis()) {
            final T task = runnable.getTask();
            doBeforeUpdate.accept(task);
            runnable.getTaskDao().doUpdate(task);
            prevSaveTsThreadLocal.set(curTs);
            return true;
        } else {
            return false;
        }
    }
}
