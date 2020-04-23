package com.bcd.sys.task;

import java.io.Serializable;

public interface Task extends Serializable{

    void setStatus(int status);


    Serializable getId();

    /**
     * 创建时触发方法
     * 在数据库保存前执行
     */
    default void onCreated(){

    }

    /**
     * 任务开始时触发方法
     * 在数据库保存前执行
     */
    default void onStarted(){

    }

    /**
     * 任务成功时触发方法
     * 在数据库保存前执行
     */
    default void onSucceed(){

    }

    /**
     * 任务失败时触发方法
     * 在数据库保存前执行
     * @param ex
     */
    default void onFailed(Exception ex){

    }

    /**
     * 任务取消时触发方法
     * 在数据库保存前执行
     */
    default void onCanceled(){

    }

    /**
     * 任务停止中时触发方法
     * 在数据库保存前执行
     */
    default void onStopping(){

    }

    /**
     * 任务停止时触发方法
     * 在数据库保存前执行
     */
    default void onStopped(){

    }




}
