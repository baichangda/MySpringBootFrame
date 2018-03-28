package com.bcd.config.shiro;

import com.bcd.base.util.SerializerUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

import java.io.*;


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
        //在这里创建session到redis中
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
            byte[] sessionBytes=null;
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
        Long res=0L;
        if(res==0){
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
    }

}
