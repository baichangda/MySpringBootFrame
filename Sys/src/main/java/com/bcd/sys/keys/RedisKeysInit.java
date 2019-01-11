package com.bcd.sys.keys;

import com.bcd.base.security.RSASecurity;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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
            ApplicationContext applicationContext= contextRefreshedEvent.getApplicationContext();
            RedisTemplate redisTemplate=(RedisTemplate) applicationContext.getBean("string_serializable_redisTemplate");
            Object[] keys=(Object[])redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
            //3、如果redis中公钥私钥为空,则生成一份,插入进去
            if(keys==null){
                keys= RSASecurity.generateKey();
                //3.1、如果插入失败,则说明在生成过程中redis中已经被存储了一份,此时再取出redis公钥私钥
                boolean res=redisTemplate.opsForValue().setIfAbsent(KeysConst.REDIS_KEY_NAME,keys);
                if(!res){
                    keys=(Object[])redisTemplate.opsForValue().get(KeysConst.REDIS_KEY_NAME);
                }
            }
            //4、最后设置此虚拟机公钥私钥
            KeysConst.PUBLIC_KEY=(RSAPublicKey)keys[0];
            KeysConst.PRIVATE_KEY=(RSAPrivateKey) keys[1];
            KeysConst.PUBLIC_KEY_BASE64= Base64.encodeBase64String(KeysConst.PUBLIC_KEY.getEncoded());
            KeysConst.PRIVATE_KEY_BASE64= Base64.encodeBase64String(KeysConst.PRIVATE_KEY.getEncoded());
        }

    }
}
