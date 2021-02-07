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
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

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

    protected RedisSerializer<V> valueSerializer;

    protected BoundListOperations<String, byte[]> boundListOperations;

    protected ThreadPoolExecutor consumePool;

    protected ExecutorService workPool;

    protected RedisTemplate<String, byte[]> redisTemplate;

    private volatile boolean stop;

    public RedisQueueMQ(String name, RedisConnectionFactory redisConnectionFactory, ValueSerializerType valueSerializer) {
        this(name, redisConnectionFactory, valueSerializer, (ThreadPoolExecutor) Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1));
    }

    public RedisQueueMQ(String name, RedisConnectionFactory redisConnectionFactory, ValueSerializerType valueSerializerType, ThreadPoolExecutor consumePool, ExecutorService workPool) {
        this.name = name;
        this.stop = false;
        this.redisTemplate = RedisUtil.newString_BytesRedisTemplate(redisConnectionFactory);
        this.boundListOperations = redisTemplate.boundListOps(name);
        this.valueSerializer = getDefaultRedisSerializer(valueSerializerType);
        this.consumePool = consumePool;
        this.workPool = workPool;
    }

    public String getName() {
        return name;
    }

    private RedisSerializer getDefaultRedisSerializer(ValueSerializerType valueSerializerType) {
        switch (valueSerializerType) {
            case STRING: {
                return RedisUtil.STRING_SERIALIZER;
            }
            case SERIALIZABLE: {
                return RedisUtil.JDK_SERIALIZATION_SERIALIZER;
            }
            case JACKSON: {
                return RedisUtil.newJackson2JsonRedisSerializer(parseValueJavaType());
            }
            default: {
                throw BaseRuntimeException.getException("valueSerializerType [{}] not support", valueSerializerType);
            }
        }
    }

    private JavaType parseValueJavaType() {
        Type parentType = ClassUtil.getParentUntil(getClass(), RedisQueueMQ.class);
        return JsonUtil.getJavaType(((ParameterizedType) parentType).getActualTypeArguments()[0]);
    }

    protected byte[] compress(byte[] data) {
        return data;
    }

    protected byte[] unCompress(byte[] data) {
        return data;
    }

    public void send(V data) {
        boundListOperations.leftPush(compress(valueSerializer.serialize(data)));
    }

    public void sendBatch(List<V> dataList) {
        byte[][] bytesArr = dataList.stream().map(e -> compress(valueSerializer.serialize(e))).toArray(byte[][]::new);
        boundListOperations.leftPushAll(bytesArr);
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

    protected void onMessageFromRedis(byte[] data) {
        onMessage(valueSerializer.deserialize(unCompress(data)));
    }

    protected void start() {
        while (consumePool.getPoolSize() < consumePool.getMaximumPoolSize()) {
            consumePool.execute(() -> {
                long popTimeout = ((LettuceConnectionFactory) redisTemplate.getConnectionFactory()).getTimeout() / 2;
                while (!stop) {
                    try {
                        byte[] data = boundListOperations.rightPop(popTimeout, TimeUnit.MILLISECONDS);
                        if (data != null) {
                            workPool.execute(() -> {
                                try {
                                    onMessageFromRedis(data);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (ex instanceof QueryTimeoutException) {
                            logger.error("redisQueueMQ queue[" + name + "] QueryTimeoutException", ex);
                        } else {
                            logger.error("redisQueueMQ queue[" + name + "] error,try after 10s", ex);
                            try {
                                Thread.sleep(10000L);
                            } catch (InterruptedException e) {
                                logger.error("redisQueueMQ queue[" + name + "] interrupted,exit ...", ex);
                                break;
                            }
                        }
                    }
                }
                logger.info("redisQueueMQ queue[{}] stop", name);
            });
        }
    }

    protected void destroy() {
        unWatch();
        consumePool.shutdown();
        workPool.shutdown();
    }
}
