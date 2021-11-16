package com.bcd.sys.task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CommonConst {
    /**
     * 用来执行系统任务的线程池
     */
    public final static ThreadPoolExecutor[] pools = new ThreadPoolExecutor[4] ;
    static {
        for (int i = 0; i < pools.length; i++) {
            pools[i]= (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        }
    }
    public final static ExecutorChooser executorChooser= ExecutorChooser.getChooser();

    /**
     * 用来存储任务对应的结果集(供关闭使用)
     */
    public final static ConcurrentHashMap<String, TaskRunnable<? extends Task>> taskIdToRunnable = new ConcurrentHashMap<>();


}
