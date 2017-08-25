package com.config.shiro;

import com.alibaba.fastjson.JSONObject;
import com.zd.downlinkcontrol.redis.RedisOp;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

import java.io.Serializable;


/**
 * Created by Administrator on 2017/8/25.
 */
public class MySessionRedisDAO extends EnterpriseCacheSessionDAO {
    /**
     * 创建session
     * @param session
     * @return
     */
    protected Serializable doCreate(Session session) {
        Serializable sessionId= super.doCreate(session);
        RedisOp.getSessionOp().setBytes(sessionId.toString(), JSONObject.toJSONBytes(session));
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
            session= JSONObject.parseObject(RedisOp.getSessionOp().getBytes(sessionId.toString()),Session.class);
        }
        return session;
    }

    /**
     * 更新session
     * @param session
     */
    protected void doUpdate(Session session) {
        super.doUpdate(session);
        RedisOp.getSessionOp().setBytes(session.getId().toString(), JSONObject.toJSONBytes(session));
    }

    /**
     * 删除session
     * @param session
     */
    protected void doDelete(Session session) {
        RedisOp.getSessionOp().remove(session.getId().toString());
    }

}
