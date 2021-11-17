package com.bcd.sys.task;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.Serializable;
import java.util.Arrays;

public class StopSysTaskListener<T extends Task<K>, K extends Serializable> extends RedisTopicMQ<String[]> {

    TaskBuilder<T,K> taskBuilder;

    public StopSysTaskListener(String name, RedisConnectionFactory connectionFactory,TaskBuilder<T,K> taskBuilder) {
        super(connectionFactory,1,1, ValueSerializerType.JACKSON, RedisUtil.doWithKey("stopSysTask:" + name));
        this.taskBuilder=taskBuilder;
    }

    @Override
    public void onMessage(String[] ids) {
        //依次停止每个任务,将结束的任务记录到结果map中
        for (String id : ids) {
            TaskRunnable runnable = taskBuilder.taskIdToRunnable.get(id);
            if (runnable != null) {
                runnable.stop();
            }
        }
    }

}
