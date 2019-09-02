package com.bcd.base.config.redis.mq.queue;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class RedisQueueMQ<V> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String name;

    protected RedisTemplate<String, V> redisTemplate;

    protected ThreadPoolExecutor consumePool;

    protected ExecutorService workPool;

    private volatile boolean stop;

    public RedisQueueMQ(String name, RedisConnectionFactory redisConnectionFactory, ValueSerializerType valueSerializer) {
        this(name, redisConnectionFactory, valueSerializer, (ThreadPoolExecutor) Executors.newFixedThreadPool(1), Executors.newCachedThreadPool());
    }

    public RedisQueueMQ(String name, RedisConnectionFactory redisConnectionFactory, ValueSerializerType valueSerializer, ThreadPoolExecutor consumePool, ExecutorService workPool) {
        this.name = name;
        this.stop = false;
        this.redisTemplate = getDefaultRedisTemplate(redisConnectionFactory, valueSerializer);
        this.consumePool = consumePool;
        this.workPool = workPool;
    }

    public String getName() {
        return name;
    }

    private JavaType parseValueJavaType() {
        Type parentType = ClassUtil.getParentUntil(getClass(), RedisQueueMQ.class);
        return JsonUtil.getJavaType(((ParameterizedType) parentType).getActualTypeArguments()[0]);
    }

    private RedisTemplate getDefaultRedisTemplate(RedisConnectionFactory redisConnectionFactory, ValueSerializerType valueSerializer) {
        switch (valueSerializer) {
            case STRING: {
                return RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
            }
            case SERIALIZABLE: {
                return RedisUtil.newString_SerializableRedisTemplate(redisConnectionFactory);
            }
            case JACKSON: {
                return RedisUtil.newString_JacksonBeanRedisTemplate(redisConnectionFactory, parseValueJavaType());
            }
            default: {
                throw BaseRuntimeException.getException("Not Support");
            }
        }
    }

    public void send(V data) {
        redisTemplate.opsForList().leftPush(name, data);
    }

    public void sendBatch(List<V> dataList) {
        redisTemplate.opsForList().leftPushAll(name, dataList);
    }

    public void watch() {
        this.stop = false;
        start();
    }

    public void unWatch() {
        this.stop = true;
    }

    public void onMessage(V data) {
        logger.info(data.toString());
    }

    protected void start() {
        while (consumePool.getPoolSize() < consumePool.getMaximumPoolSize()) {
            consumePool.execute(() -> {
                ListOperations<String, V> listOperations = redisTemplate.opsForList();
                long timeout = ((LettuceConnectionFactory) redisTemplate.getConnectionFactory()).getTimeout();
                while (!stop) {
                    try {
                        Object[] data = new Object[1];
                        try {
                            data[0] = listOperations.rightPop(name, timeout / 2, TimeUnit.MILLISECONDS);
                        } catch (QueryTimeoutException ex) {
                            logger.error("RedisQueueMQ[" + getClass().getName() + "] Schedule Task QueryTimeoutException", ex);
                        }
                        if (data[0] != null) {
                            workPool.execute(() -> {
                                try {
                                    onMessage((V) data[0]);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("Redis Queue[" + name + "] Cycle Error", e);
                        return;
                    }
                }
            });
        }
    }
}
