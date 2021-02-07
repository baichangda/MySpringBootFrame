package com.bcd.base.config.redis.mq.topic;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RedisTopicMQ<V> {

    protected Logger logger = LoggerFactory.getLogger(RedisTopicMQ.class);

    protected String[] names;

    protected RedisSerializer<V> redisSerializer;

    protected RedisTemplate<String, byte[]> redisTemplate;

    protected RedisMessageListenerContainer redisMessageListenerContainer;

    protected MessageListener messageListener;

    public RedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer, ValueSerializerType valueSerializerType, String... names) {
        this.names = names;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.redisTemplate = RedisUtil.newString_BytesRedisTemplate(redisMessageListenerContainer.getConnectionFactory());
        this.redisSerializer = getDefaultRedisSerializer(valueSerializerType);
        this.messageListener = getMessageListener();
    }

    public String[] getNames() {
        return names;
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

    public void watch() {
        this.redisMessageListenerContainer.addMessageListener(this.messageListener, Arrays.stream(this.names).map(ChannelTopic::new).collect(Collectors.toList()));
    }

    public void unWatch() {
        this.redisMessageListenerContainer.removeMessageListener(this.messageListener);
    }

    public void onMessage(V data) {
        logger.info(data.toString());
    }
}
