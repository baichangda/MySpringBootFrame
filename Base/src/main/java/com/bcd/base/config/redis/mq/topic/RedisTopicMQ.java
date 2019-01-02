package com.bcd.base.config.redis.mq.topic;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.redis.mq.RedisMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class RedisTopicMQ<V> implements RedisMQ<V> {

    protected Logger logger= LoggerFactory.getLogger(RedisTopicMQ.class);

    protected String name;

    protected RedisTemplate<String,V> redisTemplate;

    protected RedisMessageListenerContainer redisMessageListenerContainer;

    protected MessageListener messageListener;

    public RedisTopicMQ(String name, RedisMessageListenerContainer redisMessageListenerContainer, RedisTemplate<String,V> redisTemplate) {
        this.name = name;
        this.redisMessageListenerContainer=redisMessageListenerContainer;
        this.redisTemplate=redisTemplate;
        this.messageListener=getMessageListener();
    }

    public RedisTopicMQ(String name, RedisMessageListenerContainer redisMessageListenerContainer,Class<V> clazz) {
        this.name = name;
        this.redisMessageListenerContainer=redisMessageListenerContainer;
        this.redisTemplate=getDefaultRedisTemplate(redisMessageListenerContainer,clazz);
        this.messageListener=getMessageListener();
    }

    private RedisTemplate getDefaultRedisTemplate(RedisMessageListenerContainer redisMessageListenerContainer,Class<V> clazz){
        if(String.class.isAssignableFrom(clazz)){
            return RedisUtil.newString_StringRedisTemplate(redisMessageListenerContainer.getConnectionFactory());
        }else if(Serializable.class.isAssignableFrom(clazz)){
            return RedisUtil.newString_SerializableRedisTemplate(redisMessageListenerContainer.getConnectionFactory());
        } else{
            return RedisUtil.newString_JacksonBeanRedisTemplate(redisMessageListenerContainer.getConnectionFactory(),clazz);
        }
    }

    protected void onMessage(Message message, byte[] pattern){
        V v=(V)redisTemplate.getValueSerializer().deserialize(message.getBody());
        onMessage(v);
    }

    private MessageListener getMessageListener(){
        return (message,pattern)->{
            try {
                onMessage(message,pattern);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        };
    }

    @Override
    public void send(V data) {
        redisTemplate.convertAndSend(name,data);
    }

    @Override
    public void sendBatch(List<V> dataList) {
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
