package com.bcd.sys.task.cluster;

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
public class StopSysTaskResultListener implements MessageListener{
    public StopSysTaskResultListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        redisMessageListenerContainer.addMessageListener(this,new ChannelTopic(TaskConst.STOP_SYS_TASK_RESULT_CHANNEL));
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] bytes) {
        try {
            //1、解析接收到的停止结果
            JsonNode jsonNode= JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(message.getBody());
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
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }

    }
}
