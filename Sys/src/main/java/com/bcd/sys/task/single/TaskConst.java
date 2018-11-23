package com.bcd.sys.task.single;

import java.util.concurrent.*;

public class TaskConst {
    /**
     * 用来执行系统任务的线程池
     */
    public static ExecutorService SYS_TASK_POOL= Executors.newFixedThreadPool(2);
}
