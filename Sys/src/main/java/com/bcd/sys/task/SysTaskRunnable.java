package com.bcd.sys.task;


import com.bcd.base.util.ExceptionUtil;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.function.Consumer;

public class SysTaskRunnable implements Runnable{
    private final static Logger logger=LoggerFactory.getLogger(SysTaskRunnable.class);

    private TaskBean taskBean;

    private Consumer<TaskBean> consumer;

    private TaskService taskService;



    public SysTaskRunnable(TaskBean taskBean, Consumer<TaskBean> consumer, TaskService taskService) {
        this.taskBean = taskBean;
        this.consumer=consumer;
        this.taskService=taskService;
    }

    @Override
    public void run() {
        //1、先确认结果集map中是否已经添加进去
        while(true) {
            boolean hasFuture=CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.containsKey(taskBean.getId());
            if(hasFuture){
                break;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                //忽略打断等待结果加入future结果集的请求
                Thread.currentThread().interrupt();
            }
        }
        //2、如果检测到已经打断,则移除future并直接返回(因为步骤1的打断是忽略的)
        if(Thread.interrupted()){
            CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.remove(taskBean.getId());
            return;
        }
        //3、先更新任务状态为执行中
        taskBean.setStatus(TaskStatus.EXECUTING.getStatus());
        taskBean.setStartTime(new Date());
        taskService.save(taskBean);
        //4、开始执行任务;并记录执行结果
        try {
            consumer.accept(taskBean);
            taskBean.setStatus(TaskStatus.FINISHED.getStatus());
            taskBean.setFinishTime(new Date());
            taskService.save(taskBean);
        }catch (Exception e){
            if(e instanceof InterruptedException){
                //4.1、如果任务是被打断的,不进行任务处理
            }else {
                //4.2、否则当作任务失败
                logger.error("Execute [" + taskBean.getName() + "] ", e);
                taskBean.setStatus(TaskStatus.FAILED.getStatus());
                taskBean.setRemark(e.getMessage());
                taskBean.setFinishTime(new Date());
                taskBean.setStackMessage(ExceptionUtil.getStackTraceMessage(e));
                taskService.save(taskBean);
            }
        } finally {
            //5、最后从当前服务器任务id和结果映射结果集中移除
            CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.remove(taskBean.getId());
        }
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }
}
