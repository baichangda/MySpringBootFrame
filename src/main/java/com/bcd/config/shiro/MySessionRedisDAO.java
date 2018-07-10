package com.bcd.config.shiro;

import com.bcd.base.util.SerializerUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class MySessionRedisDAO extends EnterpriseCacheSessionDAO {

    private final static long TIME_OUT_SECONDS=15L;

    private ValueOperations<byte[],byte[]> redisOp;

    public MySessionRedisDAO(RedisTemplate redisTemplate) {
        redisOp=redisTemplate.opsForValue();
    }

    /**
     * 创建session
     * @param session
     * @return
     */
    protected Serializable doCreate(Session session) {
        Serializable sessionId= super.doCreate(session);
        //在这里创建session到redis中
        redisOp.set(sessionId.toString().getBytes(),SerializerUtil.serialize(session),TIME_OUT_SECONDS,TimeUnit.SECONDS);
        return sessionId;
    }

    /**
     * 读取session
     * @param sessionId
     * @return
     */
    protected Session doReadSession(Serializable sessionId) {
        Session session= super.doReadSession(sessionId);
        if(session==null){
            //在这里从redis中获取session(当内存中找不到sessionId的session时候)
            byte[] sessionBytes= redisOp.get(sessionId.toString().getBytes());
            if(sessionBytes==null){
                return null;
            }else{
                session= SerializerUtil.deserialize(sessionBytes);
            }
        }
        return session;
    }

    /**
     * 每个该session的请求都会调用
     * 更新session
     * @param session
     */
    protected void doUpdate(Session session) {
        super.doUpdate(session);
        //这里更新redis用户信息过期时间
        Boolean res=(Boolean)redisOp.getOperations().execute(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.set(session.getId().toString().getBytes(),SerializerUtil.serialize(session),Expiration.seconds(TIME_OUT_SECONDS), RedisStringCommands.SetOption.SET_IF_PRESENT);
            }
        });
        if(!res){
            session.stop();
        }
    }

    /**70f36102-07ed-48dd-a64e-ce7de3860012
     * 删除session
     * @param session
     */
    protected void doDelete(Session session) {
        super.doDelete(session);
        //这里从redis里面移除
        redisOp.getOperations().delete(session.getId().toString().getBytes());
    }

}
