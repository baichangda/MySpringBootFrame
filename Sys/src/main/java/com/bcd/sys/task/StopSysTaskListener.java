package com.bcd.sys.task.cluster;

import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.bcd.sys.task.CommonConst;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class StopSysTaskListener extends RedisTopicMQ<JsonNode>{

    public StopSysTaskListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(TaskConst.STOP_SYS_TASK_CHANNEL, redisMessageListenerContainer, JsonNode.class);
        watch();
    }

    @Override
    public void onMessage(JsonNode jsonNode) {
        //1、接收到停止任务的请求后,先解析
        String code=jsonNode.get("code").asText();
        Boolean mayInterruptIfRunning=jsonNode.get("mayInterruptIfRunning").asBoolean();
        //2、依次停止每个任务,将结束的任务记录到结果map中
        Map<Long,Boolean> resultMap=new HashMap<>();
        jsonNode.get("ids").forEach(e->{
            Long id=e.asLong();
            Future future= CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.get(id);
            if(future==null){
                return;
            }
            boolean cancelRes=future.cancel(mayInterruptIfRunning);
            resultMap.put(id,cancelRes);
        });
        //3、推送结果map和code到redis中,给请求的源服务器接收
        if(resultMap.size()>0){
            redisTemplate.convertAndSend(TaskConst.STOP_SYS_TASK_RESULT_CHANNEL,new HashMap<String,Object>(){{
                put("result",resultMap);
                put("code",code);
            }});
        }
    }
}
