package com.bcd.base.support_task.cluster;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_task.*;
import com.bcd.base.support_task.TaskBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterTaskBuilder<T extends Task<K>, K extends Serializable> extends TaskBuilder<T, K> {

    ConcurrentHashMap<String, HashMap<String, String>> requestIdToResultMap = new ConcurrentHashMap<>();

    //cluster属性
    RedisConnectionFactory connectionFactory;
    TaskRedisQueue<T, K> taskRedisQueue;
    StopTaskResultListener<T, K> stopTaskResultListener;
    StopTaskListener<T, K> stopTaskListener;

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
        this.stopTaskResultListener = new StopTaskResultListener<>(name, connectionFactory, this);
        this.stopTaskListener = new StopTaskListener<>(name, connectionFactory, this);
        this.taskRedisQueue = new TaskRedisQueue<>(name, connectionFactory, this);
        taskRedisQueue.init();
        stopTaskListener.init();
    }

    public void destroy() {
        super.destroy();
        if (taskRedisQueue != null) {
            taskRedisQueue.destroy();
        }
        if (stopTaskListener != null) {
            stopTaskListener.destroy();
        }
    }

    public K registerTask(T task, TaskFunction<T, K> function, Object... params) {
        final T t = onCreated(task);
        TaskRunnable<T, K> runnable = new TaskRunnable<>(task, function, params, this);
        taskRedisQueue.send(runnable);
        return t.getId();
    }

    public StopResult[] stopTask(K... ids) {
        StopResult[] stopResults = new StopResult[ids.length];
        if (ids.length > 0) {
            //先移除队列中正在等待的任务
            Map<String, Integer> redisIdToIndex = new HashMap<>();
            List<String> redisIdList = new ArrayList<>();
            final boolean[] remove = taskRedisQueue.remove(ids);
            for (int i = 0; i < remove.length; i++) {

                if (remove[i]) {
                    stopResults[i] = StopResult.CANCEL_SUCCEED;
                    T task = taskDao.doRead(ids[i]);
                    onCanceled(task);
                } else {
                    final String id = ids[i].toString();
                    redisIdToIndex.put(id, i);
                    redisIdList.add(id);
                }
            }
            //然后广播处理正在执行的任务
            String[] redisIdArr = new String[redisIdList.size()];
            final HashMap<String, String> redisResMap = new HashMap<>();
            for (int i = 0; i < redisIdList.size(); i++) {
                final String id = redisIdList.get(i);
                redisIdArr[i] = id;
                redisResMap.put(id, null);
            }
            //生成停止的请求用于广播
            final String requestId = RandomStringUtils.randomAlphabetic(32);
            StopRequest stopRequest = new StopRequest(requestId, redisIdArr);
            //将结果存储在map中
            requestIdToResultMap.put(requestId, redisResMap);
            //广播
            stopTaskListener.send(stopRequest);
            //最大等待一定时间、每次接收到停止的结果时候会激活
            final long total = 30000;
            final long t1 = Instant.now().toEpochMilli();
            try {
                //锁住结果map
                synchronized (redisResMap) {
                    while (true) {
                        final long spend = Instant.now().toEpochMilli() - t1;
                        if (spend >= total) {
                            //超过最大等待时间、打断循环
                            break;
                        } else {
                            //等待结果激活
                            redisResMap.wait(total - spend);
                            //激活后检查所有id都有结果、如果都有了退出循环
                            if (redisResMap.values().stream().allMatch(Objects::nonNull)) {
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException ex) {
                throw BaseRuntimeException.getException(ex);
            } finally {
                requestIdToResultMap.remove(requestId);
            }

            //将收集到的结果赋值返回
            redisResMap.forEach((k, v) -> {
                stopResults[redisIdToIndex.get(k)] = StopResult.from(Integer.parseInt(v));
            });

            //最后处理异常情况
            for (int i = 0; i < stopResults.length; i++) {
                if (stopResults[i] == null) {
                    stopResults[i] = StopResult.EXCEPTION;
                }
            }
        }
        return stopResults;

    }

    public ConcurrentHashMap<String, HashMap<String, String>> getRequestIdToResultMap() {
        return requestIdToResultMap;
    }
}
