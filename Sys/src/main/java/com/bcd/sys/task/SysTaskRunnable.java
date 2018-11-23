package com.bcd.sys.task.cluster;


import com.bcd.base.util.ExceptionUtil;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

public class SysTaskRunnable implements Runnable,Serializable{
    private final static Logger logger=LoggerFactory.getLogger(SysTaskRunnable.class);

    private TaskBean taskBean;

    public SysTaskRunnable(TaskBean taskBean) {
        this.taskBean = taskBean;
    }

    @Override
    public void run() {
        //1、先更新任务状态为执行中
        taskBean.setStatus(TaskStatus.EXECUTING.getStatus());
        taskBean.setStartTime(new Date());
        TaskUtil.Init.taskService.save(taskBean);
        //2、开始执行任务;并记录执行结果
        try {
            taskBean.getConsumer().accept(taskBean);
            taskBean.setStatus(TaskStatus.FINISHED.getStatus());
            taskBean.setFinishTime(new Date());
            TaskUtil.Init.taskService.save(taskBean);
        }catch (Exception e){
            if(e instanceof InterruptedException){
                //2.1、如果任务是被打断的,则只更新任务完成时间
                taskBean.setFinishTime(new Date());
                taskBean.setStatus(TaskStatus.STOPPED.getStatus());
                TaskUtil.Init.taskService.save(taskBean);
            }else {
                //2.2、否则当作任务失败
                logger.error("执行任务[" + taskBean.getName() + "]出现异常", e);
                taskBean.setStatus(TaskStatus.FAILED.getStatus());
                taskBean.setRemark(e.getMessage());
                taskBean.setFinishTime(new Date());
                taskBean.setStackMessage(ExceptionUtil.getStackTraceMessage(e));
                TaskUtil.Init.taskService.save(taskBean);
            }
        } finally {
            //3、最后从当前服务器任务id和结果映射结果集中移除
            TaskConst.SYS_TASK_ID_TO_FUTURE_MAP.remove(taskBean.getId().toString());
        }
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }
}
