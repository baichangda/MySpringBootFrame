package com.bcd.sys.task;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.cluster.StopSysTaskListener;
import com.bcd.sys.task.cluster.SysTaskRedisQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger logger = LoggerFactory.getLogger(TaskUtil.class);

    private static TaskDAO taskDAO;
    private static SysTaskRedisQueue sysTaskRedisQueue;
    private static StopSysTaskListener stopSysTaskListener;

    @Autowired
    public void init(TaskDAO taskDAO, SysTaskRedisQueue sysTaskRedisQueue, StopSysTaskListener stopSysTaskListener) {
        TaskUtil.taskDAO = taskDAO;
        TaskUtil.sysTaskRedisQueue = sysTaskRedisQueue;
        TaskUtil.stopSysTaskListener = stopSysTaskListener;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        sysTaskRedisQueue.start();
        stopSysTaskListener.watch();
    }

    /**
     * 注册任务
     *
     * @param task
     * @param functionName
     * @param params
     * @param <T>
     * @return
     */
    public static <T extends Task> Serializable registerTask_single(T task, String functionName, Object... params) {
        //初始化
        TaskRunnable runnable = new TaskRunnable(task, functionName, params);
        runnable.init();
        try {
            return CompletableFuture.supplyAsync(() -> {
                Serializable id;
                try {
                    id = TaskUtil.onCreated(task);
                } catch (Exception e) {
                    throw BaseRuntimeException.getException(e);
                }
                CommonConst.taskIdToRunnable.put(id.toString(), runnable);
                runnable.run();
                return id;
            }, runnable.getExecutor()).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 停止任务
     * 只停止本机正在执行的任务
     *
     * @param ids
     * @return
     */
    public static void stopTask_single(Serializable... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        for (Serializable id : ids) {
            TaskRunnable runnable = CommonConst.taskIdToRunnable.get(id.toString());
            runnable.stop();
        }
    }

    /**
     * 注册任务(redis队列任务模式)
     *
     * @param task
     * @param functionName
     * @param params
     * @param <T>
     * @return
     */
    public static <T extends Task> Serializable registerTask_cluster(T task, String functionName, Object... params) {
        Serializable id;
        try {
            TaskRunnable<T> runnable = new TaskRunnable<>(task, functionName, params);
            id = TaskUtil.onCreated(task);
            sysTaskRedisQueue.send(runnable);
        } catch (Exception e) {
            throw BaseRuntimeException.getException(e);
        }
        return id;
    }

    /**
     * 终止任务(redis队列任务模式)
     *
     * @param ids
     * @return
     */
    public static void stopTask_cluster(Serializable... ids) {
        //先移除队列中正在等待的任务
        LinkedHashMap<Serializable, Boolean> resMap = sysTaskRedisQueue.remove(ids);
        //先执行从队列中移除任务的回调
        List<Serializable> failedIdList = new LinkedList<>();
        resMap.forEach((k, v) -> {
            if (v) {
                Task task = taskDAO.doRead(k);
                TaskUtil.onCanceled(task);
            } else {
                failedIdList.add(k);
            }
        });
        //然后广播处理正在执行的任务
        String[] idArr = failedIdList.stream().map(Object::toString).toArray(String[]::new);
        stopSysTaskListener.send(idArr);
    }


    @Autowired
    public TaskUtil(TaskDAO taskDAO) {
        TaskUtil.taskDAO = taskDAO;
    }

    public static Serializable onCreated(Task task) {
        try {
            task.setStatus(TaskStatus.WAITING.getStatus());
            task.onCreated();
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCreate error", e);
        }
        return taskDAO.doCreate(task);
    }

    public static void onStarted(Task task) {
        try {
            task.setStatus(TaskStatus.EXECUTING.getStatus());
            task.onStarted();
            taskDAO.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStart error", e);
        }
    }

    public static void onSucceed(Task task) {
        try {
            task.setStatus(TaskStatus.SUCCEED.getStatus());
            task.onSucceed();
            taskDAO.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onSucceed error", e);
        }
    }

    public static void onFailed(Task task, Exception ex) {
        try {
            task.setStatus(TaskStatus.FAILED.getStatus());
            task.onFailed(ex);
            taskDAO.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onFailed error", e);
        }
    }

    public static void onCanceled(Task task) {
        try {
            task.setStatus(TaskStatus.CANCELED.getStatus());
            task.onCanceled();
            taskDAO.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCanceled error", e);
        }
    }

    public static void onStopped(Task task) {
        try {
            task.setStatus(TaskStatus.STOPPED.getStatus());
            task.onStopped();
            taskDAO.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStop error", e);
        }
    }

    public static void selectExecutor(ThreadPoolExecutor[] pools){

    }
}
