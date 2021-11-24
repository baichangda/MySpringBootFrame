package com.bcd.sys.support_task.cluster;

import com.bcd.sys.support_task.*;
import com.bcd.sys.support_task.TaskBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ClusterTaskBuilder<T extends Task<K>, K extends Serializable> extends TaskBuilder<T, K> {

    //cluster属性
    RedisConnectionFactory connectionFactory;
    SysTaskRedisQueue<T, K> sysTaskRedisQueue;
    StopSysTaskListener stopSysTaskListener;

    protected ClusterTaskBuilder(String name, TaskDao<T, K> taskDao, int poolSize, RedisConnectionFactory connectionFactory) {
        super(name, taskDao, poolSize);
        this.connectionFactory = connectionFactory;
    }


    public static <T extends Task<K>, K extends Serializable> ClusterTaskBuilder<T, K> newInstance(String name, TaskDao<T, K> taskDao, int poolSize, RedisConnectionFactory connectionFactory) {
        return new ClusterTaskBuilder<>(name, taskDao, poolSize, connectionFactory);
    }


    @Override
    public void init() {
        super.init();
        //初始化集群组件
        this.stopSysTaskListener = new StopSysTaskListener(name, connectionFactory, this);
        this.sysTaskRedisQueue = new SysTaskRedisQueue<>(name, connectionFactory, this);
        sysTaskRedisQueue.init();
        stopSysTaskListener.init();
    }

    public void destroy() {
        super.destroy();
        if (sysTaskRedisQueue != null) {
            sysTaskRedisQueue.destroy();
        }
        if (stopSysTaskListener != null) {
            stopSysTaskListener.destroy();
        }
    }

    public K registerTask(T task, TaskFunction<T, K> function, Object... params) {
        final T t = onCreated(task);
        TaskRunnable<T, K> runnable = new TaskRunnable<>(task, function, params, this);
        sysTaskRedisQueue.send(runnable);
        return t.getId();
    }

    public void stopTask(K... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
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
    }

}
