package com.bcd.sys.define;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonConst {
    public final static Long ADMIN_ID=1L;
    public final static boolean IS_PASSWORD_ENCODED=false;
    public final static String INITIAL_PASSWORD ="123qwe";


    public final static ExecutorService SYS_TASK_POOL= Executors.newWorkStealingPool();
}
