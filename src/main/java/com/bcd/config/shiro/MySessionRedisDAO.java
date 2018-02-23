package com.bcd.config.shiro;

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
            //在这里从redis中获取session
        }
        return session;
    }

    /**
     * 更新session
     * @param session
     */
    protected void doUpdate(Session session) {
        super.doUpdate(session);
        //每个该session的请求都会调用
        sessionToByte(session);
    }

    /**
     * 删除session
     * @param session
     */
    protected void doDelete(Session session) {
        super.doDelete(session);
    }


    /**
     * 把session对象转化为byte保存到redis中
     * @param session
     * @return
     */
    public byte[] sessionToByte(Session session){
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(session);
            bytes = bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 把byte还原为session
     * @param bytes
     * @return
     */
    public Session byteToSession(byte[] bytes){
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream in;
        Session session = null;
        try {
            in = new ObjectInputStream(bi);
            session = (Session) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return session;
    }
}
