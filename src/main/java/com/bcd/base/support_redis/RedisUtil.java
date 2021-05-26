package com.bcd.base.redis;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class RedisUtil {

    public final static RedisSerializer<Object> JDK_SERIALIZATION_SERIALIZER = RedisSerializer.java();
    public final static RedisSerializer<String> STRING_SERIALIZER = RedisSerializer.string();
    public final static RedisSerializer<byte[]> BYTE_ARRAY_SERIALIZER = RedisSerializer.byteArray();
    public static String SYSTEM_REDIS_KEY_PRE = "bcd:";

    /**
     * 在redis key前面加上系统标识、避免和其他服务公用redis时候因为相同的key导致异常
     *
     * @param key
     * @return
     */
    public static String doWithKey(String key) {
        return SYSTEM_REDIS_KEY_PRE + key;
    }

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
        redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setValueSerializer(redisSerializer);
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
        redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setValueSerializer(RedisSerializer.byteArray());
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
            redisSerializer = new Jackson2JsonRedisSerializer<>((Class) type);
        } else if (type instanceof JavaType) {
            redisSerializer = new Jackson2JsonRedisSerializer<>((JavaType) type);
        } else {
            throw BaseRuntimeException.getException("Param Type[{0}] Not Support", type.getTypeName());
        }
        redisSerializer.setObjectMapper(JsonUtil.GLOBAL_OBJECT_MAPPER);
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
        redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setValueSerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashValueSerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取String_Serializable的RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static <V> RedisTemplate<String, V> newString_SerializableRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.setHashValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
