package com.bcd.config.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;

import java.io.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class MySessionRedisDAO extends EnterpriseCacheSessionDAO {

    private final static long TIME_OUT_SECONDS=30*60L;

    private ValueOperations redisOp;

    public MySessionRedisDAO(RedisTemplate redisTemplate) {
        redisOp=redisTemplate.opsForValue();
    }

    /**
     * 创建session
     * @param session
     * @return
     */
    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId= super.doCreate(session);
        //在这里创建session到redis中
        redisOp.set(sessionId,session,TIME_OUT_SECONDS,TimeUnit.SECONDS);
        return sessionId;
    }

    /**
     * 读取session
     * @param sessionId
     * @return
     */
    @Override
    protected Session doReadSession(Serializable sessionId) {
        Session session= super.doReadSession(sessionId);
        if(session==null){
            //在这里从redis中获取session(当内存中找不到sessionId的session时候)
            session= (Session)redisOp.get(sessionId);
        }
        return session;
    }

    /**
     * 每个该session的请求都会调用
     * 更新session
     * @param session
     */
    @Override
    protected void doUpdate(Session session) {
        super.doUpdate(session);
        //这里更新redis用户信息过期时间
        Boolean res=(Boolean)redisOp.getOperations().execute((RedisConnection connection)->
             connection.set(redisOp.getOperations().getKeySerializer().serialize(session.getId()),redisOp.getOperations().getValueSerializer().serialize(session),Expiration.seconds(TIME_OUT_SECONDS), RedisStringCommands.SetOption.SET_IF_PRESENT)
        );
        if(!res){
            //设置session立即过期
            session.setTimeout(0L);
        }
    }

    /**70f36102-07ed-48dd-a64e-ce7de3860012
     * 删除session
     * @param session
     */
    @Override
    protected void doDelete(Session session) {
        super.doDelete(session);
        //这里从redis里面移除
        redisOp.getOperations().delete(session.getId().toString().getBytes());
    }

}
