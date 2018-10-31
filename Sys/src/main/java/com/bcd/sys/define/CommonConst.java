package com.bcd.sys.define;


import java.util.concurrent.ThreadPoolExecutor;

public class CommonConst {
    public final static Long ADMIN_ID=1L;
    public final static boolean IS_PASSWORD_ENCODED=false;
    public final static String INITIAL_PASSWORD ="123qwe";

    public final static String SYS_TASK_LIST_NAME="sysTaskQueue";

    /**
     * 用来执行系统任务的线程池
     */
    public static ThreadPoolExecutor SYS_TASK_POOL;

}
