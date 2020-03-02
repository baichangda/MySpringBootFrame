package com.bcd.config.shiro;

import com.bcd.base.config.redis.RedisUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DelegatingSession;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;

import java.io.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class RedisSessionDAO extends EnterpriseCacheSessionDAO {

    Logger logger= LoggerFactory.getLogger(RedisSessionDAO.class);

    private final static long TIME_OUT_SECONDS=60*30L;

    private RedisTemplate redisTemplate;

    private final static String SHIRO_SESSION_KEY_PRE="shiro:";

    public RedisSessionDAO(RedisConnectionFactory redisConnectionFactory) {
        redisTemplate = RedisUtil.newString_SerializableRedisTemplate(redisConnectionFactory);
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
        Serializable sessionId= super.doCreate(session);
        //在这里创建session到redis中
        redisTemplate.opsForValue().set(getKey(sessionId),session,TIME_OUT_SECONDS,TimeUnit.SECONDS);
        return sessionId;
    }

    /**
     * 读取session
     * @param sessionId
     * @return
     */
    @Override
    public Session doReadSession(Serializable sessionId) {
        logger.info("get from redis");
        Session session=super.doReadSession(sessionId);
        if(session==null) {
            session=(Session) redisTemplate.opsForValue().get(getKey(sessionId));
        }
        cache(session,sessionId);
        return session;
    }

    /**
     * 每个该session的请求都会调用
     * 更新session
     * @param session
     */
    @Override
    public void doUpdate(Session session) throws UnknownSessionException{
        super.doUpdate(session);
        redisTemplate.opsForValue().set(getKey(session.getId()),session,TIME_OUT_SECONDS,TimeUnit.SECONDS);
    }

    /**70f36102-07ed-48dd-a64e-ce7de3860012
     * 删除session
     * @param session
     */
    @Override
    public void doDelete(Session session) {
        super.doDelete(session);
        //这里从redis里面移除
        redisTemplate.delete(getKey(session.getId()).toString().getBytes());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keySet= redisTemplate.keys(SHIRO_SESSION_KEY_PRE+"*");
        return redisTemplate.opsForValue().multiGet(keySet);
    }
}
