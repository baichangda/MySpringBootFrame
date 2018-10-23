package com.bcd.config.redis.mq.queue;

import com.bcd.config.redis.mq.RedisMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class RedisQueueMQ implements RedisMQ{

    protected final static RedisSerializer DEFAULT_KEY_SERIALIZER=new StringRedisSerializer();
    protected final static RedisSerializer DEFAULT_VALUE_SERIALIZER=new Jackson2JsonRedisSerializer(Object.class);

    protected Logger logger=LoggerFactory.getLogger(RedisQueueMQ.class);

    protected String name;

    protected RedisTemplate redisTemplate;

    private volatile boolean stop;

    public RedisQueueMQ(@NotNull String name,@NotNull RedisTemplate redisTemplate) {
        this.name=name;
        this.stop=false;
        this.redisTemplate=redisTemplate;
    }

    public RedisQueueMQ(@NotNull String name, @NotNull RedisConnectionFactory redisConnectionFactory){
        this.name=name;
        this.stop=false;
        this.redisTemplate=getDefaultRedisTemplate(redisConnectionFactory);
    }

    private RedisTemplate getDefaultRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate=new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(DEFAULT_KEY_SERIALIZER);
        redisTemplate.setHashKeySerializer(DEFAULT_KEY_SERIALIZER);
        redisTemplate.setValueSerializer(DEFAULT_VALUE_SERIALIZER);
        redisTemplate.setHashValueSerializer(DEFAULT_VALUE_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Override
    public void send(Object data) {
        redisTemplate.opsForList().leftPush(name,data);
    }

    @Override
    public void sendBatch(List dataList) {
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
                long timeout=((LettuceConnectionFactory)redisQueueMQ.redisTemplate.getConnectionFactory()).getClientConfiguration().getCommandTimeout().getSeconds();
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
                        redisQueueMQ.logger.error(e.getMessage(), e);
                    }
                }
            });
        }
    }

}
