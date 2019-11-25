package com.bcd.config.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class RedisSessionRedisDAO extends AbstractSessionDAO {

    private final static long TIME_OUT_SECONDS=30L;

    private ValueOperations redisOp;

    private final static String SHIRO_SESSION_KEY_PRE="shiro:";

    public RedisSessionRedisDAO(RedisTemplate redisTemplate) {
        redisOp=redisTemplate.opsForValue();
    }

    private String getKey(Serializable sessionId){
        return SHIRO_SESSION_KEY_PRE+sessionId.toString();
    }

    /**
     * 创建session
     * @param session
     * @return
     */
    @Override
    public Serializable doCreate(Session session) {
        Serializable sessionId= generateSessionId(session);
        assignSessionId(session,sessionId);
        //在这里创建session到redis中
        redisOp.set(getKey(sessionId),session,TIME_OUT_SECONDS,TimeUnit.SECONDS);
        return sessionId;
    }

    /**
     * 读取session
     * @param sessionId
     * @return
     */
    @Override
    public Session doReadSession(Serializable sessionId) {
        //在这里从redis中获取session(当内存中找不到sessionId的session时候)
        Session session= (Session)redisOp.get(getKey(sessionId));
        return session;
    }

    /**
     * 每个该session的请求都会调用
     * 更新session
     * @param session
     */
    @Override
    public void update(Session session) throws UnknownSessionException{
        redisOp.getOperations().execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.expire(redisOp.getOperations().getKeySerializer().serialize(getKey(session.getId())),TIME_OUT_SECONDS);
                return null;
            }
        });
    }

    /**70f36102-07ed-48dd-a64e-ce7de3860012
     * 删除session
     * @param session
     */
    @Override
    public void delete(Session session) {
        //这里从redis里面移除
        redisOp.getOperations().delete(getKey(session.getId()).toString().getBytes());
    }


    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keySet= redisOp.getOperations().keys(SHIRO_SESSION_KEY_PRE+"*");
        return redisOp.multiGet(keySet);
    }
}
