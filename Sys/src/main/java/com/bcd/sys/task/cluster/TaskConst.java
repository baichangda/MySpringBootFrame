package com.bcd.sys.task.cluster;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskConst {
    public final static String SYS_TASK_LIST_NAME="sysTaskQueue";

    /**
     * 用来接收 停止系统任务 通道名
     */
    public final static String STOP_SYS_TASK_CHANNEL="stopSysTaskChannel";
    /**
     * 用来接收 停止系统任务结果 通道名
     */
    public final static String STOP_SYS_TASK_RESULT_CHANNEL="stopSysTaskResultChannel";
    /**
     * 用来唤醒请求线程
     * key: 停止任务请求的id
     * value: 当前请求的结果集map
     */
    public final static ConcurrentHashMap<String,ConcurrentHashMap<Long,Boolean>> SYS_TASK_CODE_TO_RESULT_MAP =new ConcurrentHashMap<>();
    /**
     * 用来执行系统任务的线程池
     */
    public static ThreadPoolExecutor SYS_TASK_POOL;
}
