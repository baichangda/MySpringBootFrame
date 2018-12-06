package com.bcd.base.config.redis.mq.topic;

import com.bcd.base.config.redis.mq.RedisMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@SuppressWarnings("unchecked")
public abstract class RedisTopicMQ implements RedisMQ<Message> {

    protected final static RedisSerializer DEFAULT_KEY_SERIALIZER=new StringRedisSerializer();
    protected final static RedisSerializer DEFAULT_VALUE_SERIALIZER=new Jackson2JsonRedisSerializer(Object.class);

    protected Logger logger= LoggerFactory.getLogger(RedisTopicMQ.class);

    protected String name;

    protected RedisTemplate redisTemplate;

    protected RedisMessageListenerContainer redisMessageListenerContainer;

    protected MessageListener messageListener;

    public RedisTopicMQ( String name, RedisMessageListenerContainer redisMessageListenerContainer, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisMessageListenerContainer=redisMessageListenerContainer;
        this.redisTemplate=redisTemplate;
        this.messageListener=getMessageListener();
    }

    public RedisTopicMQ(String name, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.name = name;
        this.redisMessageListenerContainer=redisMessageListenerContainer;
        this.redisTemplate=getDefaultRedisTemplate(redisMessageListenerContainer);
        this.messageListener=getMessageListener();
    }

    private RedisTemplate getDefaultRedisTemplate(RedisMessageListenerContainer redisMessageListenerContainer){
        RedisTemplate redisTemplate=new RedisTemplate();
        redisTemplate.setConnectionFactory(redisMessageListenerContainer.getConnectionFactory());
        redisTemplate.setKeySerializer(DEFAULT_KEY_SERIALIZER);
        redisTemplate.setHashKeySerializer(DEFAULT_KEY_SERIALIZER);
        redisTemplate.setValueSerializer(DEFAULT_VALUE_SERIALIZER);
        redisTemplate.setHashValueSerializer(DEFAULT_VALUE_SERIALIZER);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private MessageListener getMessageListener(){
        return (message,pattern)->{
            try {
                onMessage(message);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        };
    }

    @Override
    public void send(Object data) {
        redisTemplate.convertAndSend(name,data);
    }

    @Override
    public void sendBatch(List dataList) {
        dataList.forEach(data->send(data));
    }

    @Override
    public void watch() {
        this.redisMessageListenerContainer.addMessageListener(this.messageListener,new ChannelTopic(this.name));
    }

    @Override
    public void unWatch() {
        this.redisMessageListenerContainer.removeMessageListener(this.messageListener);
    }
}
