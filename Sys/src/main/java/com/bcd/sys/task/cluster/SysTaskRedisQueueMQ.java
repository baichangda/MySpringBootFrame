package com.bcd.sys.task.cluster;

import com.bcd.base.config.redis.mq.queue.RedisQueueMQ;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
public class SysTaskRedisQueueMQ extends RedisQueueMQ{
    public SysTaskRedisQueueMQ(@Qualifier(value = "string_jdk_redisTemplate") RedisTemplate redisTemplate) {
        super(TaskConst.SYS_TASK_LIST_NAME, redisTemplate);
        watch();
    }

    @Override
    public void onMessage(Object data) {
        //1、接收任务
        TaskBean taskBean=(TaskBean)data;
        //2、启动任务并放入结果集中
        Future future= CommonConst.SYS_TASK_POOL.submit(new SysTaskRunnable(taskBean));
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(taskBean.getId(),future);
    }
}
