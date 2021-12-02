package com.bcd.base.support_task;

import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskBuilder<T extends Task<K>, K extends Serializable> {

    private final static HashMap<String, TaskBuilder> storage = new HashMap<>();

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final TaskDao<T, K> taskDao;

    protected final String name;

    //线程属性
    protected final int poolSize;
    protected ThreadPoolExecutor[] pools;
    protected ExecutorChooser executorChooser;

    //任务id和任务对应
    protected final ConcurrentHashMap<String, TaskRunnable<T, K>> taskIdToRunnable = new ConcurrentHashMap<>();

    protected TaskBuilder(String name, TaskDao<T, K> taskDao, int poolSize) {
        this.name = name;
        this.taskDao = taskDao;
        this.poolSize = poolSize;
    }

    public static <T extends Task<K>, K extends Serializable> TaskBuilder<T, K> from(String name) {
        return storage.get(name);
    }

    public static <T extends Task<K>, K extends Serializable> TaskBuilder<T, K> newInstance(String name, TaskDao<T, K> taskDao, int poolSize) {
        return new TaskBuilder<>(name, taskDao, poolSize);
    }

    public void init() {
        //检查名称
        synchronized (storage) {
            if (storage.containsKey(name)) {
                throw BaseRuntimeException.getException("TaskBuilder[{}] [{}] exist", name, storage.get(name));
            } else {
                storage.put(name, this);
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
    }


    public void destroy() {
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

    public K registerTask(T task, TaskFunction<T, K> function, Object... params) {
        final T t = onCreated(task);
        //初始化
        TaskRunnable<T, K> runnable = new TaskRunnable<>(task, function, params, this);
        runnable.init();
        taskIdToRunnable.put(task.getId().toString(), runnable);
        runnable.getExecutor().execute(runnable);
        return t.getId();

    }

    public StopResult[] stopTask(K... ids) {
        StopResult[] stopResults = new StopResult[ids.length];
        if (ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                TaskRunnable<T, K> runnable = taskIdToRunnable.get(ids[i].toString());
                if (runnable != null) {
                    logger.info("stop{},{}", ids[i], runnable.getExecutor());
                    stopResults[i] = runnable.stop();
                }
            }
        }
        return stopResults;
    }

    protected T onCreated(T task) {
        try {
            task.setStatus(TaskStatus.WAITING.getStatus());
            task.onCreated();
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCreate error", e);
        }
        return taskDao.doCreate(task);
    }

    protected T onStarted(T task) {
        try {
            task.setStatus(TaskStatus.EXECUTING.getStatus());
            task.onStarted();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStart error", e);
            return task;
        }
    }

    protected T onSucceed(T task) {
        try {
            task.setStatus(TaskStatus.SUCCEED.getStatus());
            task.onSucceed();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onSucceed error", e);
            return task;
        }
    }

    protected T onFailed(T task, Exception ex) {
        try {
            task.setStatus(TaskStatus.FAILED.getStatus());
            task.onFailed(ex);
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onFailed error", e);
            return task;
        }
    }

    protected T onCanceled(T task) {
        try {
            task.setStatus(TaskStatus.CANCELED.getStatus());
            task.onCanceled();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onCanceled error", e);
            return task;
        }
    }

    protected T onStopped(T task) {
        try {
            task.setStatus(TaskStatus.STOPPED.getStatus());
            task.onStopped();
            return taskDao.doUpdate(task);
        } catch (Exception e) {
            logger.error("task[" + task.getId() + "] execute onStop error", e);
            return task;
        }
    }

    public TaskDao<T, K> getTaskDao() {
        return taskDao;
    }

    public String getName() {
        return name;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public ThreadPoolExecutor[] getPools() {
        return pools;
    }

    public ExecutorChooser getExecutorChooser() {
        return executorChooser;
    }

    public ConcurrentHashMap<String, TaskRunnable<T, K>> getTaskIdToRunnable() {
        return taskIdToRunnable;
    }
}
