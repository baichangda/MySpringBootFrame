package com.bcd.base.config.redis;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class RedisUtil {

    public final static JdkSerializationRedisSerializer JDK_SERIALIZATION_SERIALIZER = new JdkSerializationRedisSerializer();
    public final static StringRedisSerializer STRING_SERIALIZER = StringRedisSerializer.UTF_8;

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
        Jackson2JsonRedisSerializer<V> redisSerializer=newJackson2JsonRedisSerializer(type);
        redisSerializer.setObjectMapper(JsonUtil.GLOBAL_OBJECT_MAPPER);
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取string_bytes的redisTemplate
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String,byte[]> newString_BytesRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(null);
        redisTemplate.setHashValueSerializer(null);
        return redisTemplate;
    }

    /**
     * 获取对应的jackson序列化
     * @param type
     * @param <V>
     * @return
     */
    public static <V>Jackson2JsonRedisSerializer<V> newJackson2JsonRedisSerializer(Type type){
        Jackson2JsonRedisSerializer<V> redisSerializer;
        if (type instanceof Class) {
            redisSerializer = new Jackson2JsonRedisSerializer<>((Class) type);
        } else if (type instanceof JavaType) {
            redisSerializer = new Jackson2JsonRedisSerializer<>((JavaType) type);
        } else {
            throw BaseRuntimeException.getException("Param Type[" + type.getTypeName() + "] Not Support");
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
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(STRING_SERIALIZER);
        redisTemplate.setHashValueSerializer(STRING_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取String_Serializable的RedisTemplate
     *
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String, Serializable> newString_SerializableRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.setHashValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
