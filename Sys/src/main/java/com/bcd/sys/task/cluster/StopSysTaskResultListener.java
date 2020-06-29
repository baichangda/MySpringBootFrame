package com.bcd.sys.task.cluster;

import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@SuppressWarnings("unchecked")
@Component
public class StopSysTaskResultListener extends RedisTopicMQ<StopSysTaskResult>{


    public StopSysTaskResultListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON, ClusterTaskUtil.STOP_SYS_TASK_RESULT_CHANNEL);
        watch();
    }

    @Override
    public void onMessage(StopSysTaskResult data) {
        //解析接收到的停止结果
        String code=data.getCode();
        //找到对应的请求,并将此次的结果集合并到请求的总结果集中
        StopSysTaskContext stopSysTaskContext= ClusterTaskUtil.STOP_SYS_TASK_CODE_TO_CONTEXT_MAP.get(code);
        if(stopSysTaskContext==null){
            //如果结果集为null,说明结果超过了设定的超时时间,忽略处理结果
            return;
        }
        stopSysTaskContext.getResult().putAll(data.getResult());
        //最后唤醒主线程
        synchronized (stopSysTaskContext){
            stopSysTaskContext.notifyAll();
        }
    }
}
