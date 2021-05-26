package com.bcd.base.support_redis.mq;

import com.bcd.base.support_redis.RedisUtil;

import java.lang.reflect.Type;

public enum ValueSerializerType {
    /**
     * {@link RedisUtil#BYTE_ARRAY_SERIALIZER}
     */
    BYTE_ARRAY,
    /**
     * {@link RedisUtil#STRING_SERIALIZER}
     */
    STRING,
    /**
     * {@link RedisUtil#newJackson2JsonRedisSerializer(Type)}
     */
    JACKSON,
    /**
     * {@link RedisUtil#JDK_SERIALIZATION_SERIALIZER}
     */
    SERIALIZABLE
}