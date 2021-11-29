package com.bcd.base.support_task.cluster;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.base.support_task.Task;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.util.Optional;

public class StopTaskResultListener<T extends Task<K>, K extends Serializable> extends RedisTopicMQ<StopResultRequest> {

    private final ClusterTaskBuilder<T, K> taskBuilder;

    public StopTaskResultListener(String name, RedisConnectionFactory connectionFactory, ClusterTaskBuilder<T, K> taskBuilder) {
        super(connectionFactory, 1, 1, ValueSerializerType.JACKSON, RedisUtil.doWithKey("stopTaskResult:" + name));
        this.taskBuilder = taskBuilder;
    }

    @Override
    public void onMessage(StopResultRequest stopRequest) {
        final String requestId = stopRequest.getRequestId();
        Optional.ofNullable(taskBuilder.getRequestIdToResultMap().get(requestId)).ifPresent(e -> {
            synchronized (e) {
                e.putAll(stopRequest.getResMap());
                e.notify();
            }
        });
    }

    public ClusterTaskBuilder<T, K> getTaskBuilder() {
        return taskBuilder;
    }
}
