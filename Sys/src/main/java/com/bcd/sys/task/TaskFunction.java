package com.bcd.sys.task;


public interface TaskFunction<T extends Task> {
    /**
     * task执行任务内容
     *
     * @param context 上下文环境
     */
    void apply(TaskContext<T> context);

    default boolean supportShutdown(TaskContext<T> context) {
        return false;
    }

    default void shutdown(TaskContext<T> context) {
        //do nothing
    }
}
