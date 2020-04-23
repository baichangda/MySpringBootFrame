package com.bcd.sys.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.Future;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil {

    private final static Logger logger= LoggerFactory.getLogger(TaskUtil.class);

    private static TaskDAO taskDAO;


    @Autowired
    public TaskUtil(TaskDAO taskDAO) {
        TaskUtil.taskDAO=taskDAO;
    }

    public static Serializable onCreated(Task task){
        try {
            task.setStatus(TaskStatus.WAITING.getStatus());
            task.onCreated();
        }catch (Exception e){
            logger.error("task["+task.getId()+"] execute onCreate error",e);
        }
        return taskDAO.doCreate(task);
    }

    public static void onStarted(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.EXECUTING.getStatus());
                task.onStarted();
                taskDAO.doUpdate(task);
            } catch (Exception e) {
                logger.error("task[" + task.getId() + "] execute onStart error", e);
            }
        });
    }

    public static void onSucceed(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.SUCCEED.getStatus());
                task.onSucceed();
                taskDAO.doUpdate(task);
            } catch (Exception e) {
                logger.error("task[" + task.getId() + "] execute onSucceed error", e);
            }
        });
    }

    public static void onFailed(Task task,Exception ex){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.FAILED.getStatus());
                task.onFailed(ex);
                taskDAO.doUpdate(task);
            } catch (Exception e) {
                logger.error("task[" + task.getId() + "] execute onFailed error", e);
            }
        });
    }

    public static void onCanceled(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.CANCELED.getStatus());
                task.onCanceled();
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onCanceled error",e);
            }
        });
    }

    public static void onStopping(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.STOPPING.getStatus());
                task.onStopping();
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onStop error",e);
            }
        });
    }

    public static void onStopped(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            try {
                task.setStatus(TaskStatus.STOPPED.getStatus());
                task.onStopped();
                taskDAO.doUpdate(task);
            } catch (Exception e) {
                logger.error("task[" + task.getId() + "] execute onStop error", e);
            }
        });
    }

    public static void onException(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()-> {
            task.setStatus(TaskStatus.EXCEPTION.getStatus());
            taskDAO.doUpdate(task);
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable1=()->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("===========1");
        };
        Runnable runnable2=()->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("===========2");
        };
        Future f1= CommonConst.SYS_TASK_POOL.submit(runnable1);
        Future f2=CommonConst.SYS_TASK_POOL.submit(runnable2);
        Thread.sleep(2000);
        boolean res1= CommonConst.SYS_TASK_POOL.remove(runnable1);
        boolean res2=CommonConst.SYS_TASK_POOL.remove(runnable2);
        Thread.sleep(10000);
        boolean res3=CommonConst.SYS_TASK_POOL.remove(runnable1);
        boolean res4=CommonConst.SYS_TASK_POOL.remove(runnable2);
        System.out.println(res1+" "+res2+" "+res3+" "+res4);
    }
}
