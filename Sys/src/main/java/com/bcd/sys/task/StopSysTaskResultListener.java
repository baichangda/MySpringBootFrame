package com.bcd.sys.task;

import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
@Component
public class StopSysTaskResultListener extends RedisTopicMQ<StopSysTaskResult>{
    public StopSysTaskResultListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON,CommonConst.STOP_SYS_TASK_RESULT_CHANNEL);
        watch();
    }

    @Override
    public void onMessage(StopSysTaskResult data) {
        //1、解析接收到的停止结果
        String code=data.getCode();
        //2、找到对应的请求,并将此次的结果集合并到请求的总结果集中
        ConcurrentHashMap<String,Boolean> resultMap= CommonConst.SYS_TASK_CODE_TO_RESULT_MAP.get(code);
        if(resultMap==null){
            //2.1、如果结果集为null,说明结果超过了设定的超时时间,忽略处理结果
            return;
        }
        resultMap.putAll(data.getResult());
        //3、最后唤醒主线程
        synchronized (resultMap){
            resultMap.notifyAll();
        }
    }
}
