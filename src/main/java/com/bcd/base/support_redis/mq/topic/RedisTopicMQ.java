package com.bcd.base.support_redis.mq.topic;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RedisTopicMQ<V> {

    protected Logger logger = LoggerFactory.getLogger(RedisTopicMQ.class);

    private final RedisConnectionFactory connectionFactory;

    private final int subscriptionThreadNum;

    private final int taskThreadNum;
    private final String[] names;

    private RedisSerializer<V> redisSerializer;

    private RedisTemplate<String, byte[]> redisTemplate;

    private RedisMessageListenerContainer redisMessageListenerContainer;

    private MessageListener messageListener;

    private ThreadPoolExecutor taskExecutor;

    private ThreadPoolExecutor subscriptionExecutor;

    private boolean consumerAvailable;

    public RedisTopicMQ(RedisConnectionFactory connectionFactory, int subscriptionThreadNum, int taskThreadNum, ValueSerializerType valueSerializerType, String... names) {
        this.connectionFactory = connectionFactory;
        this.subscriptionThreadNum = subscriptionThreadNum;
        this.taskThreadNum = taskThreadNum;
        this.names = names;

        redisTemplate = RedisUtil.newString_BytesRedisTemplate(connectionFactory);
        redisSerializer = getDefaultRedisSerializer(valueSerializerType);

    }

    public String[] getNames() {
        return names;
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
                throw BaseRuntimeException.getException("Not Support");
            }
        }
    }

    private JavaType parseValueJavaType() {
        Type parentType = ClassUtil.getParentUntil(getClass(), RedisTopicMQ.class);
        return JsonUtil.getJavaType(((ParameterizedType) parentType).getActualTypeArguments()[0]);
    }

    protected void onMessage(Message message, byte[] pattern) {
        V v = redisSerializer.deserialize(unCompress(message.getBody()));
        onMessage(v);
    }

    private MessageListener getMessageListener() {
        return (message, pattern) -> {
            try {
                onMessage(message, pattern);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        };
    }

    public void send(V data, String... names) {
        byte[] bytes = compress(redisSerializer.serialize(data));
        if (names == null || names.length == 0) {
            if (this.names.length == 1) {
                redisTemplate.convertAndSend(this.names[0], bytes);
            } else {
                throw BaseRuntimeException.getException("Param[names] Can't Be Empty");
            }
        } else {
            for (String name : names) {
                redisTemplate.convertAndSend(name, bytes);
            }
        }
    }

    protected byte[] compress(byte[] data) {
        return data;
    }

    protected byte[] unCompress(byte[] data) {
        return data;
    }

    public void init() {
        if (!consumerAvailable) {
            synchronized (this) {
                if (!consumerAvailable) {
                    taskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(taskThreadNum);
                    subscriptionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(subscriptionThreadNum);
                    redisMessageListenerContainer = new RedisMessageListenerContainer();
                    redisMessageListenerContainer.setConnectionFactory(connectionFactory);
                    redisMessageListenerContainer.setTaskExecutor(taskExecutor);
                    redisMessageListenerContainer.setSubscriptionExecutor(subscriptionExecutor);
                    redisMessageListenerContainer.afterPropertiesSet();
                    redisMessageListenerContainer.start();
                    messageListener = getMessageListener();
                    redisMessageListenerContainer.addMessageListener(this.messageListener, Arrays.stream(this.names).map(ChannelTopic::new).collect(Collectors.toList()));
                }
            }
        }
    }

    public void destroy() {
        if (consumerAvailable) {
            synchronized (this) {
                if (consumerAvailable) {
                    this.redisMessageListenerContainer.removeMessageListener(this.messageListener);
                    try {
                        redisMessageListenerContainer.destroy();
                    } catch (Exception ex) {
                        throw BaseRuntimeException.getException(ex);
                    }
                    try {
                        subscriptionExecutor.shutdown();
                        while (!subscriptionExecutor.awaitTermination(60, TimeUnit.SECONDS)) {

                        }
                        taskExecutor.shutdown();
                        while (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS)) {

                        }
                    } catch (InterruptedException ex) {
                        throw BaseRuntimeException.getException(ex);
                    }
                    consumerAvailable = false;
                }
            }
        }
    }

    public void onMessage(V data) {
        logger.info(data.toString());
    }
}
