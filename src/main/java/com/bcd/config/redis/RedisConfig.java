package com.bcd.config.redis;

import com.bcd.base.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableAutoConfiguration
@SuppressWarnings("unchecked")
public class RedisConfig {

    /**
     * 实例化
     * key 用 StringRedisSerializer
     * value 用 JdkSerializationRedisSerializer
     * 的 RedisTemplate
     * @return
     */
    @Primary
    @Bean(name = "string_jdk_redisTemplate")
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        StringRedisSerializer keySerializer=new StringRedisSerializer();
        JdkSerializationRedisSerializer valueSerializer= new JdkSerializationRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 实例化
     * key 用 StringRedisSerializer
     * value 用 Jackson2JsonRedisSerializer
     * 的 RedisTemplate
     * @return
     */
    @Bean(name = "string_jackson_redisTemplate")
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory,@Qualifier(value = "jacksonRedisSerializer") RedisSerializer valueSerializer) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        StringRedisSerializer keySerializer=new StringRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Primary
    @Bean(name = "jacksonRedisSerializer")
    public RedisSerializer redisSerializer(){
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer=new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(JsonUtil.GLOBAL_OBJECT_MAPPER);
        return jackson2JsonRedisSerializer;
    }


    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory){
        RedisMessageListenerContainer redisMessageListenerContainer= new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(factory);
        return redisMessageListenerContainer;
    }

}