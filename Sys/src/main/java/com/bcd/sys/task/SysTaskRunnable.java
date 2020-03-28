package com.bcd.sys.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
@SuppressWarnings("unchecked")
public class SysTaskRunnable<T extends Task> implements Runnable{
    private final static Logger logger= LoggerFactory.getLogger(SysTaskRunnable.class);

    private T task;

    private TaskFunction<T> function;

    private TaskDAO taskDAO;


    public SysTaskRunnable(T task, TaskFunction<T> function, TaskDAO taskDAO) {
        this.task = task;
        this.function=function;
        this.taskDAO=taskDAO;
    }

    @Override
    public void run() {
        Serializable id= task.getId();
        //1、先确认结果集map中是否已经添加进去
        while(true) {
            boolean hasFuture=CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.containsKey(id.toString());
            if(hasFuture){
                break;
            }
            try {
                //1.1、如果没有则等待
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                //1.2、忽略打断等待结果加入future结果集的请求
                Thread.currentThread().interrupt();
            }
        }
        //2、如果检测到已经打断,则移除future并直接返回(因为步骤1的打断是忽略的)
        if(Thread.interrupted()){
            CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.remove(id.toString());
            return;
        }
        //3、开始任务
        TaskUtil.onStart(task);
        //4、开始执行任务;并记录执行结果
        try {
            task=function.apply(task);
            TaskUtil.onSucceed(task);
        }catch (Exception e){
            if(e instanceof InterruptedException){
                //4.1、如果任务是被打断的
                logger.info("task[" + task.getId() + "] has been interrupted");
                TaskUtil.onStop(task);
            }else {
                //4.2、否则当作任务失败
                logger.error("execute task[" + task.getId() + "] failed", e);
                TaskUtil.onFailed(task,e);
            }
        } finally {
            //5、最后从当前服务器任务id和结果映射结果集中移除
            CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.remove(task.getId().toString());
        }
    }

    public T getTask() {
        return task;
    }


}
