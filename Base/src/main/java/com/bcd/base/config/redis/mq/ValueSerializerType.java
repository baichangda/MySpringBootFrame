package com.bcd.base.config.redis.mq;

import java.lang.reflect.Type;

public enum ValueSerializerType {
    /**
     * {@link com.bcd.base.config.redis.RedisUtil#BYTE_ARRAY_SERIALIZER}
     */
    BYTE_ARRAY,
    /**
     * {@link com.bcd.base.config.redis.RedisUtil#STRING_SERIALIZER}
     */
    STRING,
    /**
     * {@link com.bcd.base.config.redis.RedisUtil#newJackson2JsonRedisSerializer(Type)}
     */
    JACKSON,
    /**
     * {@link com.bcd.base.config.redis.RedisUtil#JDK_SERIALIZATION_SERIALIZER}
     */
    SERIALIZABLE
}