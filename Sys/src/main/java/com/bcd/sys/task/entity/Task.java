package com.bcd.sys.task.entity;

import java.io.Serializable;

public interface Task extends Serializable{

    Serializable getId();

    /**
     * 创建时触发方法
     */
    void onCreate();

    /**
     * 任务开始时触发方法
     */
    void onStart();

    /**
     * 任务停止时触发方法
     */
    void onStop();

    /**
     * 任务成功时触发方法
     */
    void onSucceed();

    /**
     * 任务失败时触发方法
     * @param ex
     */
    void onFailed(Exception ex);
}
