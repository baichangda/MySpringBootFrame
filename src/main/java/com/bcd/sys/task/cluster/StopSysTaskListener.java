package com.bcd.sys.task.cluster;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskRunnable;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;

public class StopSysTaskListener<T extends Task<K>, K extends Serializable> extends RedisTopicMQ<String[]> {

    ClusterTaskBuilder<T,K> taskBuilder;

    public StopSysTaskListener(String name, RedisConnectionFactory connectionFactory, ClusterTaskBuilder<T,K> taskBuilder) {
        super(connectionFactory,1,1, ValueSerializerType.JACKSON, RedisUtil.doWithKey("stopSysTask:" + name));
        this.taskBuilder=taskBuilder;
    }

    @Override
    public void onMessage(String[] ids) {
        //依次停止每个任务,将结束的任务记录到结果map中
        for (String id : ids) {
            TaskRunnable runnable = taskBuilder.getTaskIdToRunnable().get(id);
            if (runnable != null) {
                runnable.stop();
            }
        }
    }

}
