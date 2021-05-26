package com.bcd.sys.task.cluster;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
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
public class SysTaskRedisQueue<T extends Task, C extends ClusterTaskContext<T>> implements SpringInitializable {
    private final static Logger logger = LoggerFactory.getLogger(SysTaskRedisQueue.class);
    private String name;
    private Semaphore lock = new Semaphore(CommonConst.SYS_TASK_POOL.getMaximumPoolSize());

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
        this.name = ClusterTaskUtil.SYS_TASK_LIST_NAME;
        this.boundListOperations = redisTemplate.boundListOps(this.name);
        this.popNullIntervalInSecond = 30;
    }

    @Override
    public void init(ContextRefreshedEvent event) {
        start();
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
                        onTask((C) data);
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
     * @param context
     */
    public void onTask(C context) {
        //1、接收并解析任务数据
        T task = context.getTask();
        TaskFunction<T> taskFunction = context.getFunction();
        //2、如果找不到对应执行方法实体,则任务执行失败并抛出异常
        if (taskFunction == null) {
            BaseRuntimeException exception = BaseRuntimeException.getException("can't find clusterTaskFunction[" + context.getFunctionName() + "]");
            TaskUtil.onFailed(task, exception);
            throw exception;
        }
        //3、使用线程池执行任务
        TaskRunnable<T> runnable = new TaskRunnable(context);
        CommonConst.SYS_TASK_POOL.execute(() -> {
            try {
                //3.1、执行任务
                runnable.run();
            } finally {
                //3.2、执行完毕后释放锁
                lock.release();
            }
        });
        CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.put(task.getId().toString(), runnable);
    }

    public void send(C context) {
        boundListOperations.leftPush(context);
    }

    public LinkedHashMap<Serializable, Boolean> remove(Serializable... ids) {
        if (ids == null || ids.length == 0) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<Serializable, Boolean> resMap = new LinkedHashMap<>();
        List<C> contextList = boundListOperations.range(0L, -1L);
        for (Serializable id : ids) {
            boolean res = false;
            for (C context : contextList) {
                if (id.equals(context.getTask().getId())) {
                    Long count = boundListOperations.remove(1, context);
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

    public void destroy() {
        stop();
        if (fetchPool != null) {
            fetchPool.shutdown();
        }
        if (workPool != null) {
            workPool.shutdown();
        }
    }

    public int getPopNullIntervalInSecond() {
        return popNullIntervalInSecond;
    }

    public void setPopNullIntervalInSecond(int popNullIntervalInSecond) {
        this.popNullIntervalInSecond = popNullIntervalInSecond;
    }
}
