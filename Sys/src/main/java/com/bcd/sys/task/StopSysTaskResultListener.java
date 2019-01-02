package com.bcd.sys.task.cluster;

import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StopSysTaskResultListener extends RedisTopicMQ<JsonNode>{
    public StopSysTaskResultListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(TaskConst.STOP_SYS_TASK_RESULT_CHANNEL, redisMessageListenerContainer, JsonNode.class);
        watch();
    }

    @Override
    public void onMessage(JsonNode jsonNode) {
        //1、解析接收到的停止结果
        String code=jsonNode.get("code").asText();
        //2、找到对应的请求,并将此次的结果集合并到请求的总结果集中
        ConcurrentHashMap<Long,Boolean> resultMap= TaskConst.SYS_TASK_CODE_TO_RESULT_MAP.get(code);
        if(resultMap==null){
            //2.1、如果结果集为null,说明结果超过了设定的超时时间,忽略处理结果
            return;
        }
        jsonNode.get("result").fields().forEachRemaining(e->{
            resultMap.put(Long.parseLong(e.getKey()),e.getValue().asBoolean());
        });
        //3、最后唤醒主线程
        synchronized (resultMap){
            resultMap.notify();
        }
    }
}
