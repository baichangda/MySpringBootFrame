package com.bcd.config.redis;

import com.bcd.base.config.redis.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.Serializable;

@Configuration
@SuppressWarnings("unchecked")
public class RedisConfig {

    /**
     * key 用 StringRedisSerializer
     * value 用 JdkSerializationRedisSerializer
     * 的 RedisTemplate
     * @return
     */
    @Bean(name = "string_serializable_redisTemplate")
    public RedisTemplate<String,Serializable> string_serializable_redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return RedisUtil.newString_SerializableRedisTemplate(redisConnectionFactory);
    }

    /**
     * key 用 StringRedisSerializer
     * value 用 StringRedisSerializer
     * 的 RedisTemplate
     * @return
     */
    @Bean(name = "string_string_redisTemplate")
    public RedisTemplate string_string_redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory){
        RedisMessageListenerContainer redisMessageListenerContainer= new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(factory);
        return redisMessageListenerContainer;
    }

}