package com.bcd.sys.task.cluster;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskRunnable;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class StopSysTaskListener extends RedisTopicMQ<String[]> {
    /**
     * 用来接收 停止系统任务 通道名
     */
    public final static String STOP_SYS_TASK_CHANNEL = RedisUtil.doWithKey("stopSysTaskChannel");

    public StopSysTaskListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON, STOP_SYS_TASK_CHANNEL);
    }

    @Override
    public void onMessage(String[] ids) {
        //依次停止每个任务,将结束的任务记录到结果map中
        for (String id : ids) {
            TaskRunnable runnable = CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.get(id);
            if (runnable != null) {
                runnable.stop();
            }
        }
    }

}
