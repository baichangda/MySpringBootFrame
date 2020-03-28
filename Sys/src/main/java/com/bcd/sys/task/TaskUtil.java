package com.bcd.sys.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil {

    private final static Logger logger= LoggerFactory.getLogger(TaskUtil.class);

    private static TaskDAO taskDAO;


    @Autowired
    public TaskUtil(TaskDAO taskDAO) {
        TaskUtil.taskDAO=taskDAO;
    }

    public static Serializable onCreate(Task task){
        try {
            task.onCreate();
        }catch (Exception e){
            logger.error("task["+task.getId()+"] execute onCreate error",e);
        }
        return taskDAO.doCreate(task);
    }

    public static void onStart(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()->{
            try {
                task.onStart();
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onStart error",e);
            }
        });
    }

    public static void onFailed(Task task,Exception ex){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()->{
            try {
                task.onFailed(ex);
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onFailed error",e);
            }
        });
    }

    public static void onSucceed(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()->{
            try {
                task.onSucceed();
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onSucceed error",e);
            }
        });
    }

    public static void onStop(Task task){
        CommonConst.SYS_TASK_EVENT_POOL.execute(()->{
            try {
                task.onStop();
                taskDAO.doUpdate(task);
            }catch (Exception e){
                logger.error("task["+task.getId()+"] execute onStop error",e);
            }
        });
    }
}
