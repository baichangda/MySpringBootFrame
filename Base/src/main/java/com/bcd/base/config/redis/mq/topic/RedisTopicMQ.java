package com.bcd.base.config.redis.mq.topic;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RedisTopicMQ<V>{

    protected Logger logger= LoggerFactory.getLogger(RedisTopicMQ.class);

    protected String [] names;

    protected RedisTemplate<String,V> redisTemplate;

    protected RedisMessageListenerContainer redisMessageListenerContainer;

    protected MessageListener messageListener;

    public RedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer, ValueSerializerType valueSerializerType, String ... names) {
        this.names = names;
        this.redisMessageListenerContainer=redisMessageListenerContainer;
        this.redisTemplate=getDefaultRedisTemplate(redisMessageListenerContainer,valueSerializerType);
        this.messageListener=getMessageListener();
    }

    public String[] getNames() {
        return names;
    }

    private RedisTemplate getDefaultRedisTemplate(RedisMessageListenerContainer redisMessageListenerContainer, ValueSerializerType valueSerializerType){
        switch (valueSerializerType){
            case STRING:{
                return RedisUtil.newString_StringRedisTemplate(redisMessageListenerContainer.getConnectionFactory());
            }
            case SERIALIZABLE:{
                return RedisUtil.newString_SerializableRedisTemplate(redisMessageListenerContainer.getConnectionFactory());
            }
            case JACKSON:{
                return RedisUtil.newString_JacksonBeanRedisTemplate(redisMessageListenerContainer.getConnectionFactory(),parseValueJavaType());
            }
            default:{
                throw BaseRuntimeException.getException("Not Support");
            }
        }
    }

    private JavaType parseValueJavaType(){
        Type parentType= ClassUtil.getParentUntil(getClass(),RedisTopicMQ.class);
        return JsonUtil.getJavaType(((ParameterizedType) parentType).getActualTypeArguments()[0]);
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

    public void send(V data,String ... names) {
        if(names==null||names.length==0){
            if(this.names.length==1){
                redisTemplate.convertAndSend(this.names[0], data);
            }else{
                throw BaseRuntimeException.getException("MQ Has More Than One Topic,Param[names] Can't Be Empty");
            }
        }else {
            for (String name : names) {
                redisTemplate.convertAndSend(name, data);
            }
        }
    }

    public void watch() {
        this.redisMessageListenerContainer.addMessageListener(this.messageListener, Arrays.stream(this.names).map(ChannelTopic::new).collect(Collectors.toList()));
    }

    public void unWatch() {
        this.redisMessageListenerContainer.removeMessageListener(this.messageListener);
    }

    public void onMessage(V data) {
        logger.info(data.toString());
    }
}
