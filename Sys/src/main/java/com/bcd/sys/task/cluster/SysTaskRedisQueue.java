package com.bcd.sys.task.cluster;

import com.bcd.base.config.redis.mq.queue.RedisQueueMQ;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class SysTaskRedisQueueMQ{
    private final static Logger logger= LoggerFactory.getLogger(SysTaskRedisQueueMQ.class);
    private String name;

    @Autowired
    @Qualifier(value = "string_jdk_redisTemplate")
    private RedisTemplate redisTemplate;
    public SysTaskRedisQueueMQ() {
        this.name=TaskConst.SYS_TASK_LIST_NAME;
        Worker.init(this);
    }

    public void onMessage(Object data) {
        //1、接收任务
        TaskBean taskBean=(TaskBean)data;
        //2、启动任务并放入结果集中
        Future future= CommonConst.SYS_TASK_POOL.submit(new SysTaskRunnable(taskBean));
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(taskBean.getId(),future);
    }

    public void send(Object data) {
        redisTemplate.opsForList().leftPush(name,data);
    }

    static class Worker{
        /**
         * 从redis中遍历数据的线程池
         */
        private final static ExecutorService POOL= Executors.newCachedThreadPool();

        /**
         * 执行工作任务的线程池
         */
        private final static ExecutorService WORK_POOL=Executors.newCachedThreadPool();

        public static void init(SysTaskRedisQueueMQ redisQueueMQ){
            POOL.execute(()->{
                Semaphore lock=new Semaphore(CommonConst.SYS_TASK_POOL.getPoolSize());
                ListOperations listOperations= redisQueueMQ.redisTemplate.opsForList();
                long timeout=((LettuceConnectionFactory)redisQueueMQ.redisTemplate.getConnectionFactory()).getClientConfiguration().getCommandTimeout().getSeconds();
                while(true){
                    try {
                        lock.acquire();
                        Object data = listOperations.rightPop(redisQueueMQ.name, timeout / 2, TimeUnit.MILLISECONDS);
                        if (data == null) {
                            lock.release();
                        }else{
                            WORK_POOL.execute(() -> {
                                try {
                                    redisQueueMQ.onMessage(data);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                } finally {
                                    lock.release();
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("SysTaskRedisQueueMQ["+redisQueueMQ.name+"] Stop", e);
                        break;
                    }
                }
            });
        }
    }
}
