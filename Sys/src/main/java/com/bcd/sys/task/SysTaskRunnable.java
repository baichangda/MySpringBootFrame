package com.bcd.sys.task;


import com.bcd.sys.bean.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

public class SysTaskRunnable implements Runnable,Serializable{
    private final static Logger logger=LoggerFactory.getLogger(SysTaskRunnable.class);

    private TaskBean taskBean;

    public SysTaskRunnable(@NotNull TaskBean taskBean) {
        this.taskBean = taskBean;
    }

    @Override
    public void run() {
        //1、先更新任务状态为执行中
        taskBean.setStatus(2);
        taskBean.setStartTime(new Date());
        TaskUtil.getTaskService().save(taskBean);
        if(taskBean.getOnStart()!=null){
            try {
                taskBean.getOnStart().accept(taskBean);
            }catch (Exception e){
                logger.error("执行任务["+taskBean.getName()+"]的[onStart]出现异常",e);
            }
        }
        //2、开始执行任务;并记录执行结果
        try {
            taskBean.getConsumer().accept(taskBean);
            taskBean.setStatus(4);
            taskBean.setFinishTime(new Date());
            TaskUtil.getTaskService().save(taskBean);
            if(taskBean.getOnSuccess()!=null){
                try {
                    taskBean.getOnSuccess().accept(taskBean);
                }catch (Exception e){
                    logger.error("执行任务["+taskBean.getName()+"]的[onSuccess]出现异常",e);
                }
            }
        }catch (Exception e){
            logger.error("执行任务["+taskBean.getName()+"]出现异常",e);
            taskBean.setStatus(5);
            taskBean.setRemark(e.getMessage());
            taskBean.setFinishTime(new Date());
            TaskUtil.getTaskService().save(taskBean);
            if(taskBean.getOnFailed()!=null){
                try {
                    taskBean.getOnFailed().accept(taskBean);
                }catch (Exception ex){
                    logger.error("执行任务["+taskBean.getName()+"]的[getOnFailed]出现异常",ex);
                }
            }
        }
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }
}
