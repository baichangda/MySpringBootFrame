package com.bcd.config.shiro;

import com.bcd.base.config.redis.RedisUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.hibernate.type.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class RedisSessionDAO extends EnterpriseCacheSessionDAO {

    private final static String SHIRO_SESSION_HASH_KEY = RedisUtil.doWithKey("shiroSession");
    Logger logger = LoggerFactory.getLogger(RedisSessionDAO.class);
    private HashOperations<String, String, Session> hashOperations;

    public RedisSessionDAO(RedisConnectionFactory redisConnectionFactory) {
        hashOperations = RedisUtil.newString_SerializableRedisTemplate(redisConnectionFactory).opsForHash();
    }


    /**
     * 创建session
     *
     * @param session
     * @return
     */
    @Override
    public Serializable doCreate(Session session) {
        Serializable sessionId = super.doCreate(session);
        logger.info("doCreate from redis[{}]", sessionId);
        //在这里创建session到redis中
        hashOperations.put(SHIRO_SESSION_HASH_KEY, sessionId.toString(), session);
        return sessionId;
    }

    /**
     * 读取session
     *
     * @param sessionId
     * @return
     */
    @Override
    public Session doReadSession(Serializable sessionId) {
        logger.info("doReadSession from redis[{}]", sessionId);
        Session session = super.doReadSession(sessionId);
        if (session == null) {
            try {
                session = hashOperations.get(SHIRO_SESSION_HASH_KEY, sessionId.toString());
            } catch (SerializationException ex) {
                //用于处理修改了session data的数据结构但是redis中依然存在数据导致反序列化失败
                logger.warn("redis session data struct changed,delete it[{}]", sessionId);
                hashOperations.delete(SHIRO_SESSION_HASH_KEY, sessionId.toString());
            }
        }
        //从redis加载出来以后加到本地缓存中
        cache(session, sessionId);
        return session;
    }

    /**
     * 每个该session的请求都会调用
     * 更新session
     *
     * @param session
     */
    @Override
    public void doUpdate(Session session) throws UnknownSessionException {
        logger.info("doUpdate from redis[{}]", session.getId());
        super.doUpdate(session);
        hashOperations.put(SHIRO_SESSION_HASH_KEY, session.getId().toString(), session);
    }

    /**
     * 70f36102-07ed-48dd-a64e-ce7de3860012
     * 删除session
     *
     * @param session
     */
    @Override
    public void doDelete(Session session) {
        logger.info("doDelete from redis[{}]", session.getId());
        super.doDelete(session);
        //这里从redis里面移除
        hashOperations.delete(SHIRO_SESSION_HASH_KEY, session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        logger.info("getActiveSessions from redis");
        try {
            return new ArrayList<>(hashOperations.values(SHIRO_SESSION_HASH_KEY));
        } catch (SerializationException ex) {
            //用于处理修改了session data的数据结构但是redis中依然存在数据导致反序列化失败
            logger.warn("redis session data struct changed,delete all");
            hashOperations.getOperations().delete(SHIRO_SESSION_HASH_KEY);
            return Collections.emptyList();
        }
    }


}
