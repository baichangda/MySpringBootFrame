package com.bcd.sys.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class TaskRunnable<T extends Task> implements Runnable{
    private final static Logger logger= LoggerFactory.getLogger(TaskRunnable.class);

    private final T task;

    private final TaskFunction<T> function;

    private boolean supportShutdownRunning;

    /**
     * 1:等待中
     * 2:执行中
     * 3:执行成功
     * 4:执行失败
     * 5:被取消
     * 6:停止中
     * 7:已停止
     * 8:异常终止
     *
     * 状态变更可能性:
     * 0->1
     *
     * 1->2 //开始执行
     * 1->5 //还在等待中时调用停止方法取消
     *
     * 2->3 //执行成功
     * 2->4 //执行失败
     * 2->6 //执行中时调用停止方法
     *
     * 6->7 //任务停止
     *
     */
    final AtomicInteger status=new AtomicInteger(0);

    public TaskRunnable(T task, TaskFunction<T> function) {
        this.task = task;
        this.function=function;
        this.supportShutdownRunning =function.supportShutdown(task);
        this.status.set(1);
    }

    public TaskFunction<T> getFunction() {
        return function;
    }

    public T getTask() {
        return task;
    }

    /**
     * 1、先尝试从队列中移除
     * 2、然后尝试停止正在执行的任务
     *
     * 如果当前任务不支持supportShutdownRunning,则返回false
     * 如果支持supportShutdownRunning,此时任务状态可能为
     * 1:等待中
     * 2:执行中
     * 3:执行成功
     * 4:执行失败
     * 6:停止中
     * 7:已停止
     * 8:异常终止
     *
     * 1,2则变更为6
     * 3,4,8直接返回失败
     * 6,7直接返回成功
     */
    public boolean shutdown(){
        boolean removed=CommonConst.SYS_TASK_POOL.remove(this);
        if(removed){
            //如果移除成功了,则状态一定是1
            boolean curRes=status.compareAndSet(1,5);
            if(curRes){
                TaskUtil.onCanceled(task);
                return true;
            }else{
                logger.error("try change task status[1->5] failed,now is [{}]", status.get());
                TaskUtil.onException(task);
                return false;
            }
        }else {
            if (supportShutdownRunning) {
                int curStatus = status.get();
                if (curStatus == 3 || curStatus == 4 || curStatus == 8) {
                    return false;
                } else if (curStatus == 6 || curStatus == 7) {
                    return true;
                } else {
                    if (status.compareAndSet(2, 6)) {
                        //如果设置成功,说明已开始执行Function
                        TaskUtil.onStopping(task);
                        function.shutdown(task);
                        return true;
                    } else {
                        if (status.compareAndSet(1, 6)) {
                            //如果设置成功,说明还未开始执行Function
                            //这种情况是任务刚开始,还没有来得及变更状态1->2
                            TaskUtil.onStopping(task);
                            return true;
                        } else {
                            //如果失败,则状态异常,此时返回失败
                            logger.error("try change task status[1->6],[2->6] failed,now is [{}]", status.get());
                            TaskUtil.onException(task);
                            return false;
                        }
                    }
                }
            }else{
                return false;
            }
        }

    }

    @Override
    public void run() {
        try {
            //尝试设置状态等待中->执行中(1->2)
            if(!status.compareAndSet(1,2)){
                //尝试设置停止中->已停止(6->7)
                if(status.compareAndSet(6,7)){
                    TaskUtil.onStopped(task);
                    return;
                }else{
                    logger.error("try change task status[1->2],[6->7] failed,now is [{}]", status.get());
                    status.set(8);
                    TaskUtil.onException(task);
                }
            }
            //触发开始方法
            TaskUtil.onStarted(task);
            //执行任务
            function.apply(task);
            //尝试设置状态执行中->执行成功(2->3)
            if(status.compareAndSet(2,3)){
                TaskUtil.onSucceed(task);
            }else{
                //尝试设置状态停止中->已停止(6->7)
                //即时此时任务可能已经正常完成,但是还是标记为已停止
                if(status.compareAndSet(6,7)){
                    TaskUtil.onStopped(task);
                }else{
                    logger.error("try change task status[2->3],[6->7] failed,now is [{}]",status.get());
                    status.set(8);
                    TaskUtil.onException(task);
                }
            }
        }catch (Exception e){
            logger.error("execute task[" + task.getId() + "] failed", e);
            //尝试设置状态执行中->执行失败(2->4)
            if (status.compareAndSet(2, 4)) {
                TaskUtil.onFailed(task, e);
            } else {
                //尝试设置状态停止中->已停止(6->7)
                if (status.compareAndSet(6, 7)) {
                    TaskUtil.onStopped(task);
                } else {
                    logger.error("try change task status[2->4],[6-7] failed,now is [{}]",status.get());
                    status.set(8);
                    TaskUtil.onException(task);
                }
            }
        } finally {
            //最后从当前服务器任务id和结果映射结果集中移除
            CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.remove(task.getId().toString());
        }
    }




}
