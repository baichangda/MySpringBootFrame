package com.bcd.sys.task.cluster;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.sys.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
@Component
public class SysTaskRedisQueue<T extends Task>{

    /**
     * 集群模式中redis任务队列名称
     */
    public final static String SYS_TASK_LIST_NAME = RedisUtil.doWithKey("sysTask");

    private final static Logger logger = LoggerFactory.getLogger(SysTaskRedisQueue.class);
    private String name;
    private Semaphore lock = new Semaphore(1);

    private BoundListOperations boundListOperations;

    /**
     * 当rpop操作结果为null时候,每次rpop的间隔时间
     */
    private int popNullIntervalInSecond;

    private volatile boolean stop;

    /**
     * 从redis中遍历数据的线程池
     */
    private ExecutorService fetchPool = Executors.newSingleThreadExecutor();

    /**
     * 执行工作任务的线程池
     */
    private ExecutorService workPool = Executors.newCachedThreadPool();

    public SysTaskRedisQueue(@Qualifier("string_serializable_redisTemplate") RedisTemplate redisTemplate) {
        this.name = SYS_TASK_LIST_NAME;
        this.boundListOperations = redisTemplate.boundListOps(this.name);
        this.popNullIntervalInSecond = 30;
    }


    /**
     * 从redis list中获取任务并执行
     *
     * @throws InterruptedException
     */
    private void fetchAndExecute() throws InterruptedException {
        lock.acquire();
        try {
            Object data = boundListOperations.rightPop();
            if (data == null) {
                TimeUnit.SECONDS.sleep(popNullIntervalInSecond);
                lock.release();
            } else {
                workPool.execute(() -> {
                    try {
                        onTask((TaskRunnable<T>) data);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        lock.release();
                    }
                });
            }
        } catch (Exception ex) {
            lock.release();
            if (ex instanceof QueryTimeoutException) {
                logger.error("SysTaskRedisQueue[" + name + "] fetchAndExecute QueryTimeoutException", ex);
            } else {
                logger.error("SysTaskRedisQueue[" + name + "] fetchAndExecute error,try after 10s", ex);
                Thread.sleep(10000L);
            }
        }
    }

    /**
     * 接收到任务处理
     *
     * @param runnable
     */
    public void onTask(TaskRunnable<T> runnable) {
        //初始化环境
        runnable.init();
        runnable.getExecutor().execute(()->{
            //使用线程池执行任务
            try {
                //3.1、执行任务
                runnable.run();
            } finally {
                //3.2、执行完毕后释放锁
                lock.release();
            }
            CommonConst.taskIdToRunnable.put(runnable.getTask().getId().toString(), runnable);
        });
    }

    public void send(TaskRunnable<T> runnable) {
        boundListOperations.leftPush(runnable);
    }

    public LinkedHashMap<Serializable, Boolean> remove(Serializable... ids) {
        if (ids == null || ids.length == 0) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<Serializable, Boolean> resMap = new LinkedHashMap<>();
        List<TaskRunnable<T>> runnableList = boundListOperations.range(0L, -1L);
        for (Serializable id : ids) {
            boolean res = false;
            for (TaskRunnable<T> runnable : runnableList) {
                if (id.equals(runnable.getTask().getId())) {
                    Long count = boundListOperations.remove(1, runnable);
                    if (count != null && count == 1) {
                        res = true;
                        break;
                    } else {
                        res = false;
                        break;
                    }
                }
            }
            resMap.put(id, res);
        }
        return resMap;
    }


    public void start() {
        stop = false;
        fetchPool.execute(() -> {
            while (!stop) {
                try {
                    fetchAndExecute();
                } catch (InterruptedException ex) {
                    //处理打断情况,此时退出
                    logger.error("SysTaskRedisQueue[" + name + "] interrupted,exit...", ex);
                    break;
                }
            }
        });
    }

    public void stop() {
        stop = true;
    }

    public int getPopNullIntervalInSecond() {
        return popNullIntervalInSecond;
    }

    public void setPopNullIntervalInSecond(int popNullIntervalInSecond) {
        this.popNullIntervalInSecond = popNullIntervalInSecond;
    }
}
