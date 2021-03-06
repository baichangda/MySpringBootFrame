package com.bcd.sys.task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CommonConst {
    /**
     * 用来执行系统任务的线程池
     */
    public final static ThreadPoolExecutor SYS_TASK_POOL = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    /**
     * 用来执行on事件的线程池
     */
    public final static ThreadPoolExecutor SYS_TASK_EVENT_POOL = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);


    /**
     * 用来存储任务对应的结果集(供关闭使用)
     */
    public final static ConcurrentHashMap<String, TaskRunnable<? extends Task>> SYS_TASK_ID_TO_TASK_RUNNABLE_MAP = new ConcurrentHashMap<>();


}
