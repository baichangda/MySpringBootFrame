package com.bcd.sys.keys;

import com.bcd.base.support_spring_init.SpringInitializable;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.RSAUtil;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Base64;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@SuppressWarnings("unchecked")
@Component
public class RedisKeysInit implements SpringInitializable {
    @Override
    public void init(ContextRefreshedEvent event) {
        //1、判断是否是集群环境
        if (KeysConst.IS_CLUSTER) {
            //2、如果是集群环境则从redis中取出公钥私钥
            RedisConnectionFactory redisConnectionFactory = event.getApplicationContext().getBean(RedisConnectionFactory.class);
            RedisTemplate<String, String[]> redisTemplate = RedisUtil.newString_JacksonBeanRedisTemplate(redisConnectionFactory, TypeFactory.defaultInstance().constructArrayType(String.class));
            String[] keys = redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
            //3、如果redis中公钥私钥为空,则生成一份,插入进去
            if (keys == null) {
                Object[] objects = RSAUtil.generateKey(1024);
                keys = new String[2];
                keys[0] = Base64.getEncoder().encodeToString(((PublicKey) objects[0]).getEncoded());
                keys[1] = Base64.getEncoder().encodeToString(((PrivateKey) objects[1]).getEncoded());
                //3.1、如果插入失败,则说明在生成过程中redis中已经被存储了一份,此时再取出redis公钥私钥
                boolean res = redisTemplate.opsForValue().setIfAbsent(KeysConst.REDIS_KEY_NAME, keys);
                if (!res) {
                    keys = redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
                }
            }
            //4、最后设置此虚拟机公钥私钥
            KeysConst.PUBLIC_KEY_BASE64 = keys[0];
            KeysConst.PRIVATE_KEY_BASE64 = keys[1];
            KeysConst.PUBLIC_KEY = RSAUtil.restorePublicKey(Base64.getDecoder().decode(keys[0]));
            KeysConst.PRIVATE_KEY = RSAUtil.restorePrivateKey(Base64.getDecoder().decode(keys[1]));

        }
    }

}
