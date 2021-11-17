package com.bcd.base.support_redis.mq.queue;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class RedisQueueMQ<V> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String name;

    protected RedisSerializer<V> valueSerializer;

    protected BoundListOperations<String, byte[]> boundListOperations;

    protected ThreadPoolExecutor consumeExecutor;

    protected ThreadPoolExecutor workExecutor;

    protected RedisTemplate<String, byte[]> redisTemplate;

    protected RedisConnectionFactory connectionFactory;

    protected ValueSerializerType valueSerializerType;

    protected int consumerThreadNum;

    protected int workThreadNum;

    private volatile boolean stop;

    public RedisQueueMQ(String name, RedisConnectionFactory connectionFactory, ValueSerializerType valueSerializerType, int consumerThreadNum, int workThreadNum) {
        this.name = name;
        this.connectionFactory = connectionFactory;
        this.valueSerializerType = valueSerializerType;
        this.consumerThreadNum = consumerThreadNum;
        this.workThreadNum = workThreadNum;
    }

    public String getName() {
        return name;
    }

    private RedisSerializer getDefaultRedisSerializer(ValueSerializerType valueSerializerType) {
        switch (valueSerializerType) {
            case BYTE_ARRAY: {
                return RedisUtil.BYTE_ARRAY_SERIALIZER;
            }
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

    public void init() {
        this.stop = false;
        this.redisTemplate = RedisUtil.newString_BytesRedisTemplate(connectionFactory);
        this.boundListOperations = redisTemplate.boundListOps(name);
        this.valueSerializer = getDefaultRedisSerializer(valueSerializerType);
        this.consumeExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(consumerThreadNum);
        this.workExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(workThreadNum);
        start();
    }

    public void destroy() {
        this.stop = true;
        try {
            consumeExecutor.shutdown();
            while (!consumeExecutor.awaitTermination(60, TimeUnit.SECONDS)) {

            }
            workExecutor.shutdown();
            while (!workExecutor.awaitTermination(60, TimeUnit.SECONDS)) {

            }
        } catch (InterruptedException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }


    public void onMessage(V data) {
        logger.info(data.toString());
    }

    protected void onMessageFromRedis(byte[] data) {
        onMessage(valueSerializer.deserialize(unCompress(data)));
    }

    protected void start() {
        while (consumeExecutor.getPoolSize() < consumeExecutor.getMaximumPoolSize()) {
            consumeExecutor.execute(() -> {
                long popTimeout = ((LettuceConnectionFactory) redisTemplate.getConnectionFactory()).getTimeout() / 2;
                while (!stop) {
                    try {
                        byte[] data = boundListOperations.rightPop(popTimeout, TimeUnit.MILLISECONDS);
                        if (data != null) {
                            workExecutor.execute(() -> {
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
}
