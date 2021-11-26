package com.bcd.base.support_task.cluster;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.base.support_task.Task;
import com.bcd.base.support_task.TaskRunnable;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;

public class StopTaskListener<T extends Task<K>, K extends Serializable> extends RedisTopicMQ<StopRequest> {

    ClusterTaskBuilder<T, K> taskBuilder;

    StopTaskResultListener<T, K> stopTaskResultListener;

    public StopTaskListener(String name, RedisConnectionFactory connectionFactory, ClusterTaskBuilder<T, K> taskBuilder) {
        super(connectionFactory, 1, 1, ValueSerializerType.JACKSON, RedisUtil.doWithKey("stopTask:" + name));
        this.taskBuilder = taskBuilder;
    }

    @Override
    public void init() {
        this.stopTaskResultListener = taskBuilder.stopTaskResultListener;
        super.init();
    }

    @Override
    public void onMessage(StopRequest stopRequest) {
        //依次停止每个任务,将结束的任务记录到结果map中
        StopResultRequest stopResultRequest = new StopResultRequest(stopRequest.getRequestId());
        final String[] ids = stopRequest.getIds();
        for (String id : ids) {
            TaskRunnable runnable = taskBuilder.getTaskIdToRunnable().get(id);
            if (runnable != null) {
                stopResultRequest.getResMap().put(id, runnable.stop().getFlag() + "");
            }
        }
        stopTaskResultListener.send(stopResultRequest);
    }

}
