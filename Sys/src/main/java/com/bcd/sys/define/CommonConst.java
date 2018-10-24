package com.bcd.sys.define;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonConst {
    public final static Long ADMIN_ID=1L;
    public final static boolean IS_PASSWORD_ENCODED=false;
    public final static String INITIAL_PASSWORD ="123qwe";


    /**
     * 用来执行系统任务的线程池
     */
    public final static ExecutorService SYS_TASK_POOL= Executors.newFixedThreadPool(3);
}
