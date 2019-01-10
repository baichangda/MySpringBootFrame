package com.bcd.base.config.redis.mq.queue;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.redis.mq.RedisMQ;
import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class RedisQueueMQ<V> implements RedisMQ<V>{
    protected Logger logger=LoggerFactory.getLogger(this.getClass());

    protected String name;

    protected RedisTemplate<String,V> redisTemplate;

    private volatile boolean stop;

    public RedisQueueMQ(String name,RedisTemplate<String,V> redisTemplate) {
        this.name=name;
        this.stop=false;
        this.redisTemplate=redisTemplate;
    }

    public RedisQueueMQ(String name,RedisConnectionFactory redisConnectionFactory,Class<V> clazz,ValueSerializer valueSerializer){
        this.name=name;
        this.stop=false;
        this.redisTemplate=getDefaultRedisTemplate(redisConnectionFactory,clazz,valueSerializer);
    }

    private RedisTemplate getDefaultRedisTemplate(RedisConnectionFactory redisConnectionFactory, Class<V> clazz, ValueSerializer valueSerializer){
        switch (valueSerializer){
            case STRING:{
                return RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
            }
            case SERIALIZABLE:{
                return RedisUtil.newString_SerializableRedisTemplate(redisConnectionFactory);
            }
            case JACKSON:{
                return RedisUtil.newString_JacksonBeanRedisTemplate(redisConnectionFactory,clazz);
            }
            default:{
                throw BaseRuntimeException.getException("Not Support");
            }
        }
    }

    @Override
    public void send(V data) {
        redisTemplate.opsForList().leftPush(name,data);
    }

    @Override
    public void sendBatch(List<V> dataList) {
        redisTemplate.opsForList().leftPushAll(name,dataList);
    }

    @Override
    public void watch() {
        Worker.init(this);
    }

    @Override
    public void unWatch(){
        this.stop=true;
    }

    static class Worker{
        private Worker() {
        }

        /**
         * 从redis中遍历数据的线程池
         */
        private final static ExecutorService POOL= Executors.newCachedThreadPool();

        /**
         * 执行工作任务的线程池
         */
        private final static ExecutorService WORK_POOL=Executors.newCachedThreadPool();

        public static void init(RedisQueueMQ redisQueueMQ){
            POOL.execute(()->{
                ListOperations listOperations= redisQueueMQ.redisTemplate.opsForList();
                long timeout=((LettuceConnectionFactory)redisQueueMQ.redisTemplate.getConnectionFactory()).getTimeout();
                while(!redisQueueMQ.stop){
                    try {
                        Object data = listOperations.rightPop(redisQueueMQ.name, timeout / 2, TimeUnit.MILLISECONDS);
                        if (data != null) {
                            WORK_POOL.execute(() -> {
                                try {
                                    redisQueueMQ.onMessage(data);
                                } catch (Exception e) {
                                    redisQueueMQ.logger.error(e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        redisQueueMQ.logger.error("Redis Queue["+redisQueueMQ.name+"] Cycle Error", e);
                        return;
                    }
                }
            });
        }
    }

}
