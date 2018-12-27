package com.bcd.sys.task.cluster;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class SysTaskRedisQueue {
    private final static Logger logger= LoggerFactory.getLogger(SysTaskRedisQueue.class);
    private String name;

    private RedisTemplate redisTemplate;

    public SysTaskRedisQueue(@Qualifier(value = "string_jdk_redisTemplate") RedisTemplate redisTemplate) {
        this.name=TaskConst.SYS_TASK_LIST_NAME;
        this.redisTemplate=redisTemplate;
        Worker.init(this);
    }

    /**
     * 接收到任务数据时候处理
     * @param data
     */
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
        private final static ExecutorService POOL= Executors.newSingleThreadExecutor();

        /**
         * 执行工作任务的线程池
         */
        private final static ExecutorService WORK_POOL=Executors.newCachedThreadPool();

        public static void init(SysTaskRedisQueue sysTaskRedisQueue){
            Semaphore lock=new Semaphore(CommonConst.SYS_TASK_POOL.getMaximumPoolSize());
            POOL.execute(()->{
                ListOperations listOperations= sysTaskRedisQueue.redisTemplate.opsForList();
                long timeout=((LettuceConnectionFactory)sysTaskRedisQueue.redisTemplate.getConnectionFactory()).getClientConfiguration().getCommandTimeout().getSeconds();
                while(true){
                    try {
                        lock.acquire();
                        Object data = listOperations.rightPop(sysTaskRedisQueue.name, timeout / 2, TimeUnit.MILLISECONDS);
                        if (data == null) {
                            lock.release();
                        }else{
                            WORK_POOL.execute(() -> {
                                try {
                                    sysTaskRedisQueue.onMessage(data);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                } finally {
                                    lock.release();
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("SysTaskRedisQueue["+sysTaskRedisQueue.name+"] Stop", e);
                        break;
                    }
                }
            });
        }
    }
}
