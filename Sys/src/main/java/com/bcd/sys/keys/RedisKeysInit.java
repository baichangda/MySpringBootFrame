package com.bcd.sys.keys;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.security.RSASecurity;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@SuppressWarnings("unchecked")
@Component
public class RedisKeysInit implements ApplicationListener<ContextRefreshedEvent>{
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //1、判断是否是集群环境
        if(KeysConst.IS_CLUSTER){
            //2、如果是集群环境则从redis中取出公钥私钥
            RedisConnectionFactory redisConnectionFactory= contextRefreshedEvent.getApplicationContext().getBean(RedisConnectionFactory.class);
            RedisTemplate<String,String[]> redisTemplate= RedisUtil.newString_JacksonBeanRedisTemplate(redisConnectionFactory, TypeFactory.defaultInstance().constructArrayType(String.class));
            String[] keys=redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
            //3、如果redis中公钥私钥为空,则生成一份,插入进去
            if(keys==null){
                Object[] objects= RSASecurity.generateKey();
                keys=new String[2];
                keys[0]=Base64.encodeBase64String(((PublicKey)objects[0]).getEncoded());
                keys[1]=Base64.encodeBase64String(((PrivateKey)objects[1]).getEncoded());
                //3.1、如果插入失败,则说明在生成过程中redis中已经被存储了一份,此时再取出redis公钥私钥
                boolean res=redisTemplate.opsForValue().setIfAbsent(KeysConst.REDIS_KEY_NAME,keys);
                if(!res){
                    keys=redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
                }
            }
            //4、最后设置此虚拟机公钥私钥
            KeysConst.PUBLIC_KEY_BASE64= keys[0];
            KeysConst.PRIVATE_KEY_BASE64= keys[1];
            KeysConst.PUBLIC_KEY=RSASecurity.restorePublicKey(Base64.decodeBase64(keys[0]));
            KeysConst.PRIVATE_KEY=RSASecurity.restorePrivateKey(Base64.decodeBase64(keys[1]));

        }

    }
}
