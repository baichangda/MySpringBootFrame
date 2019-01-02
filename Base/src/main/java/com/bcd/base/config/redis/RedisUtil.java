package com.bcd.base.config.redis;

import com.bcd.base.util.JsonUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

public class RedisUtil {

    private final static JdkSerializationRedisSerializer JDK_SERIALIZATION_SERIALIZER =new JdkSerializationRedisSerializer();
    private final static StringRedisSerializer STRING_SERIALIZER =StringRedisSerializer.UTF_8;

    /**
     * 获取对应实体类型的String_Jackson的redisTemplate
     * @param redisConnectionFactory
     * @param clazz
     * @param <V>
     * @return
     */
    public static <V>RedisTemplate<String,V> newString_JacksonBeanRedisTemplate(RedisConnectionFactory redisConnectionFactory,Class<V> clazz){
        RedisTemplate<String,V> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<V> redisSerializer=new Jackson2JsonRedisSerializer<>(clazz);
        redisSerializer.setObjectMapper(JsonUtil.GLOBAL_OBJECT_MAPPER);
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取String_String的RedisTemplate
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String,String> newString_StringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate=new RedisTemplate<>();
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
     * @param redisConnectionFactory
     * @return
     */
    public static RedisTemplate<String,Serializable> newString_SerializableRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Serializable> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.setHashValueSerializer(JDK_SERIALIZATION_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
