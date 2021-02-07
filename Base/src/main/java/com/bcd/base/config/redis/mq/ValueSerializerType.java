package com.bcd.base.config.redis.mq;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import java.lang.reflect.Type;

public enum ValueSerializerType {
    /**
     * {@link org.springframework.data.redis.serializer.StringRedisSerializer#UTF_8}
     */
    STRING,
    /**
     * {@link com.bcd.base.config.redis.RedisUtil#newJackson2JsonRedisSerializer(Type)}
     */
    JACKSON,
    /**
     * {@link JdkSerializationRedisSerializer#JdkSerializationRedisSerializer()}
     */
    SERIALIZABLE
}