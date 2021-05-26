package com.bcd.sys.task.cluster;

import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StopSysTaskListener extends RedisTopicMQ<StopSysTask> {


    @Autowired
    StopSysTaskResultListener stopSysTaskResultListener;

    public StopSysTaskListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON, ClusterTaskUtil.STOP_SYS_TASK_CHANNEL);
        watch();
    }

    @Override
    public void onMessage(StopSysTask data) {
        //接收到停止任务的请求后,先解析
        String code = data.getCode();
        //依次停止每个任务,将结束的任务记录到结果map中
        Map<String, Boolean> resultMap = new HashMap<>();
        String[] ids = data.getIds();
        for (String id : ids) {
            TaskRunnable runnable = CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.get(id);
            if (runnable == null) {
                return;
            }
            resultMap.put(id, runnable.shutdown());
        }
        //推送结果map和code到redis中,给请求的源服务器接收
        if (resultMap.size() > 0) {
            StopSysTaskResult result = new StopSysTaskResult();
            result.setCode(code);
            result.setResult(resultMap);
            stopSysTaskResultListener.send(result);
        }
    }

}
