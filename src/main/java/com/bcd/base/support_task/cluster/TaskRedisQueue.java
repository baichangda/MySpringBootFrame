package com.bcd.base.support_task.cluster;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_task.Task;
import com.bcd.base.support_task.TaskRunnable;
import com.bcd.base.support_task.TaskBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class SysTaskRedisQueue<T extends Task<K>, K extends Serializable> {

    private final static Logger logger = LoggerFactory.getLogger(SysTaskRedisQueue.class);
    private String name;
    private String queueName;

    TaskBuilder<T, K> taskBuilder;

    private Semaphore semaphore;

    private BoundListOperations boundListOperations;

    private volatile boolean stop;

    /**
     * 从redis中遍历数据的线程池
     */
    private ExecutorService fetchPool = Executors.newSingleThreadExecutor();

    /**
     * 执行工作任务的线程池
     */
    private ExecutorService workPool = Executors.newCachedThreadPool();

    public SysTaskRedisQueue(String name, RedisConnectionFactory connectionFactory, TaskBuilder<T, K> taskBuilder) {
        final RedisTemplate<String, Object> redisTemplate = RedisUtil.newString_SerializableRedisTemplate(connectionFactory);
        this.name = name;
        this.queueName = RedisUtil.doWithKey("sysTask:" + name);
        this.boundListOperations = redisTemplate.boundListOps(this.queueName);
        this.taskBuilder = taskBuilder;
        this.semaphore = new Semaphore(this.taskBuilder.getPoolSize());
    }


    /**
     * 从redis list中获取任务并执行
     *
     * @throws InterruptedException
     */
    private void fetchAndExecute() throws InterruptedException {
        semaphore.acquire();
        try {
            Object data = boundListOperations.rightPop(30, TimeUnit.SECONDS);
            if (data == null) {
                semaphore.release();
            } else {
                workPool.execute(() -> {
                    try {
                        onTask((TaskRunnable<T, K>) data);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        semaphore.release();
                    }
                });
            }
        } catch (Exception ex) {
            semaphore.release();
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
    public void onTask(TaskRunnable<T, K> runnable) {
        //初始化环境
        runnable.init();
        taskBuilder.getTaskIdToRunnable().put(runnable.getTask().getId().toString(), runnable);
        runnable.getExecutor().execute(() -> {
            //使用线程池执行任务
            try {
                //3.1、执行任务
                runnable.run();
            } finally {
                //3.2、执行完毕后释放锁
                semaphore.release();
            }
        });
    }

    public void send(TaskRunnable<T, K> runnable) {
        boundListOperations.leftPush(runnable);
    }

    public LinkedHashMap<K, Boolean> remove(K... ids) {
        if (ids == null || ids.length == 0) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<K, Boolean> resMap = new LinkedHashMap<>();
        List<TaskRunnable<T, K>> runnableList = boundListOperations.range(0L, -1L);
        for (K id : ids) {
            boolean res = false;
            for (TaskRunnable<T, K> runnable : runnableList) {
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


    public void init() {
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

    public void destroy() {
        stop = true;
        fetchPool.shutdown();
        workPool.shutdown();
        try {
            while (!fetchPool.awaitTermination(60, TimeUnit.SECONDS)) {

            }
            while (!workPool.awaitTermination(60, TimeUnit.SECONDS)) {

            }
        } catch (InterruptedException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }


}
