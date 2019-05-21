package com.bcd.sys.task;

import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class StopSysTaskListener extends RedisTopicMQ<StopSysTask>{

    @Autowired
    StopSysTaskResultListener stopSysTaskResultListener;

    public StopSysTaskListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON,CommonConst.STOP_SYS_TASK_CHANNEL);
        watch();
    }

    @Override
    public void onMessage(StopSysTask data) {
        //1、接收到停止任务的请求后,先解析
        String code=data.getCode();
        Boolean mayInterruptIfRunning=data.getMayInterruptIfRunning();
        //2、依次停止每个任务,将结束的任务记录到结果map中
        Map<String,Boolean> resultMap=new HashMap<>();
        String[] ids=data.getIds();
        Arrays.stream(ids).forEach(id->{
            Future future= CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.get(id);
            if(future==null){
                return;
            }
            boolean cancelRes=future.cancel(mayInterruptIfRunning);
            resultMap.put(id,cancelRes);
        });
        //3、推送结果map和code到redis中,给请求的源服务器接收
        if(resultMap.size()>0){
            StopSysTaskResult result=new StopSysTaskResult();
            result.setCode(code);
            result.setResult(resultMap);
            stopSysTaskResultListener.send(result);
        }
    }

}
