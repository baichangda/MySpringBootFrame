package com.bcd.base.support_redis;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_redis.serializer.RedisSerializer_key_string;
import com.bcd.base.support_redis.serializer.RedisSerializer_value_integer;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unchecked")
public class RedisUtil {
    public final static String KEY_PREFIX = "bcd-";
    public final static RedisSerializer<String> SERIALIZER_KEY_STRING = new RedisSerializer_key_string(KEY_PREFIX, StandardCharsets.UTF_8);
    public final static RedisSerializer<Object> SERIALIZER_VALUE_JDK = RedisSerializer.java();
    public final static RedisSerializer<String> SERIALIZER_VALUE_STRING = RedisSerializer.string();
    public final static RedisSerializer<Integer> SERIALIZER_VALUE_INTEGER = new RedisSerializer_value_integer();
    public final static RedisSerializer<byte[]> SERIALIZER_VALUE_BYTEARRAY = RedisSerializer.byteArray();

    /**
     * 获取对应实体类型的String_Jackson的redisTemplate
     *
     * @param redisConnectionFactory
     * @param type                   必须为Class或者JavaType类型
     * @param <V>
     * @return
     */
    public static <V> RedisTemplate<String, V> newString_JacksonBeanRedisTemplate(RedisConnectionFactory redisConnectionFactory, Type type) {
        RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<V> redisSerializer = newJackson2JsonRedisSerializer(type);
        redisTemplate.setKeySerializer(RedisUtil.SERIALIZER_KEY_STRING);
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashValueSerializer(redisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取string_bytes的redisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String, byte[]> newString_BytesRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.SERIALIZER_KEY_STRING);
        redisTemplate.setValueSerializer(RedisSerializer.byteArray());
        redisTemplate.setHashKeySerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashValueSerializer(RedisSerializer.byteArray());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取对应的jackson序列化
     *
     * @param type
     * @param <V>
     * @return
     */
    public static <V> Jackson2JsonRedisSerializer<V> newJackson2JsonRedisSerializer(Type type) {
        Jackson2JsonRedisSerializer<V> redisSerializer;
        if (type instanceof Class) {
            redisSerializer = new Jackson2JsonRedisSerializer<>(JsonUtil.GLOBAL_OBJECT_MAPPER, (Class<V>) type);
        } else if (type instanceof JavaType) {
            redisSerializer = new Jackson2JsonRedisSerializer<>(JsonUtil.GLOBAL_OBJECT_MAPPER, (JavaType) type);
        } else {
            throw BaseRuntimeException.get("Param Type[{0}] Not Support", type.getTypeName());
        }
        return redisSerializer;
    }

    /**
     * 获取String_String的RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String, String> newString_StringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.SERIALIZER_KEY_STRING);
        redisTemplate.setValueSerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashKeySerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashValueSerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取String_String的RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String, Integer> newString_IntegerRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.SERIALIZER_KEY_STRING);
        redisTemplate.setValueSerializer(RedisUtil.SERIALIZER_VALUE_INTEGER);
        redisTemplate.setHashKeySerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashValueSerializer(RedisUtil.SERIALIZER_VALUE_INTEGER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取String_Serializable的RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static <V extends Serializable> RedisTemplate<String, V> newString_SerializableRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.SERIALIZER_KEY_STRING);
        redisTemplate.setValueSerializer(SERIALIZER_VALUE_JDK);
        redisTemplate.setHashKeySerializer(RedisUtil.SERIALIZER_VALUE_STRING);
        redisTemplate.setHashValueSerializer(SERIALIZER_VALUE_JDK);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
