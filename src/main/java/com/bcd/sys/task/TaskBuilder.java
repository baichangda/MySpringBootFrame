package com.bcd.sys.task;


import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

public class TaskBuilder<T extends Task<K>, K extends Serializable> {

    private final static HashMap<String, TaskBuilder> storage = new HashMap<>();

    Logger logger = LoggerFactory.getLogger(TaskBuilder.class);

    //名称
    String name;

    Mode mode = Mode.SINGLE;

    //任务类持久化
    TaskDao<T, K> taskDao;

    //cluster属性
    RedisConnectionFactory connectionFactory;
    SysTaskRedisQueue<T, K> sysTaskRedisQueue;
    StopSysTaskListener stopSysTaskListener;

    //线程属性
    int poolSize = 2;
    ThreadPoolExecutor[] pools;
    ExecutorChooser executorChooser;

    //任务id和任务对应
    ConcurrentHashMap<String, TaskRunnable<T, K>> taskIdToRunnable = new ConcurrentHashMap<>();

    public static <T extends Task<K>, K extends Serializable> TaskBuilder<T, K> from(String name) {
        return storage.get(name);
    }

    public TaskBuilder<T, K> build() {
        //检查名称
        synchronized (storage) {
            if (storage.containsKey(name)) {
                throw BaseRuntimeException.getException("TaskBuilder[{}] [{}] exist", name, storage.get(name));
            } else {
                storage.put(name,this);
            }
        }

        //初始化线程池
        pools = new ThreadPoolExecutor[poolSize];
        {
            for (int i = 0; i < poolSize; i++) {
                pools[i] = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            }
        }
        this.executorChooser = ExecutorChooser.getChooser(pools);

        //初始化集群组件
        if (mode == Mode.CLUSTER) {
            this.stopSysTaskListener = new StopSysTaskListener(name, connectionFactory, this);
            this.sysTaskRedisQueue = new SysTaskRedisQueue<>(name, connectionFactory, this);
            sysTaskRedisQueue.init();
            stopSysTaskListener.init();
        }
        return this;
    }

    public static <T extends Task<K>, K extends Serializable> TaskBuilder<T, K> newBuilder(String name,
                                                                                           TaskDao<T, K> taskDao) {
        TaskBuilder<T, K> builder = new TaskBuilder<>();
        builder.name = name;
        builder.taskDao = taskDao;
        return builder;
    }

    public TaskBuilder<T, K> withPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public TaskBuilder<T, K> withCluster(String name, RedisConnectionFactory connectionFactory) {
        this.mode = Mode.CLUSTER;
        this.name = name;
        this.connectionFactory = connectionFactory;
        return this;
    }


    public K registerTask(T task, TaskFunction<T,K> function, Object... params) {
        final T t = onCreated(task);
        switch (this.mode) {
            case SINGLE: {
                //初始化
                TaskRunnable<T, K> runnable = new TaskRunnable<>(task, function, params, this);
                runnable.init();
                runnable.getExecutor().execute(runnable);
                return t.getId();
            }
            case CLUSTER: {
                TaskRunnable<T, K> runnable = new TaskRunnable<>(task, function, params, this);
                sysTaskRedisQueue.send(runnable);
                return t.getId();
            }
            default: {
                throw BaseRuntimeException.getException("mode[{}] not support", mode);
            }
        }
    }

    public void stopTask(K... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        switch (mode) {
            case SINGLE: {
                for (Serializable id : ids) {
                    TaskRunnable<T, K> runnable = taskIdToRunnable.get(id.toString());
                    if (runnable != null) {
                        logger.info("stop{},{}", id, runnable.getExecutor());
                        runnable.stop();
                    }
                }
                return;
            }
            case CLUSTER: {
                //先移除队列中正在等待的任务
                LinkedHashMap<K, Boolean> resMap = sysTaskRedisQueue.remove(ids);
                //先执行从队列中移除任务的回调
                List<K> failedIdList = new LinkedList<>();
                resMap.forEach((k, v) -> {
                    if (v) {
                        T task = taskDao.doRead(k);
                        onCanceled(task);
                    } else {
                        failedIdList.add(k);
                    }
                });
                //然后广播处理正在执行的任务
                String[] idArr = failedIdList.stream().map(Object::toString).toArray(String[]::new);
                stopSysTaskListener.send(idArr);
                return;
            }
            default: {
                throw BaseRuntimeException.getException("mode[{}] not support", mode);
            }
        }

    }

    T onCreated(T task) {
        try {
            task.setStatus(TaskStatus.WAITING.getStatus());
            task.onCreated();
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCreate error", e);
        }
        return taskDao.doCreate(task);
    }

    T onStarted(T task) {
        try {
            task.setStatus(TaskStatus.EXECUTING.getStatus());
            task.onStarted();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStart error", e);
            return task;
        }
    }

    T onSucceed(T task) {
        try {
            task.setStatus(TaskStatus.SUCCEED.getStatus());
            task.onSucceed();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onSucceed error", e);
            return task;
        }
    }

    T onFailed(T task, Exception ex) {
        try {
            task.setStatus(TaskStatus.FAILED.getStatus());
            task.onFailed(ex);
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onFailed error", e);
            return task;
        }
    }

    T onCanceled(T task) {
        try {
            task.setStatus(TaskStatus.CANCELED.getStatus());
            task.onCanceled();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCanceled error", e);
            return task;
        }
    }

    T onStopped(T task) {
        try {
            task.setStatus(TaskStatus.STOPPED.getStatus());
            task.onStopped();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStop error", e);
            return task;
        }
    }

    public void destroy() {
        if (sysTaskRedisQueue != null) {
            sysTaskRedisQueue.destroy();
        }
        if (stopSysTaskListener != null) {
            stopSysTaskListener.destroy();
        }
        if (pools.length > 0) {
            for (ThreadPoolExecutor pool : pools) {
                pool.shutdown();
                try {
                    while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    }
                } catch (InterruptedException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            }
        }
    }

}

enum Mode {
    SINGLE,
    CLUSTER
}