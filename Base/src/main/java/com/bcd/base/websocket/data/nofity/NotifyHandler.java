package com.bcd.base.websocket.data.nofity;

import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.server.BaseWebSocket;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知处理器
 * @param <T> 服务器通知客户端的数据类型
 * @param <R> 客户端注册请求参数类型
 */
public abstract class NotifyHandler<T,R> {

    protected Logger logger= LoggerFactory.getLogger(getClass());

    public final static Map<NotifyEvent,ConcurrentHashMap<String,NotifyMessage>> EVENT_TO_SN_NOTIFY_MESSAGE_MAP=new ConcurrentHashMap<>();

    public final static Map<NotifyEvent,NotifyHandler> EVENT_TO_HANDLER_MAP=new HashMap<>();

    protected ConcurrentHashMap<String,NotifyMessage> sn_to_notify_message_map=new ConcurrentHashMap<>();


    protected NotifyEvent event;

    protected JavaType registerParamJavaType;

    public NotifyEvent getEvent() {
        return event;
    }

    public JavaType getRegisterParamJavaType() {
        return registerParamJavaType;
    }

    public NotifyHandler(NotifyEvent event) {
        this.event = event;
        Type parentType= ClassUtil.getParentUntil(getClass(),NotifyHandler.class);
        this.registerParamJavaType= JsonUtil.getJavaType(((ParameterizedType)parentType).getActualTypeArguments()[1]);
        this.sn_to_notify_message_map=EVENT_TO_SN_NOTIFY_MESSAGE_MAP.computeIfAbsent(event,(k)->new ConcurrentHashMap<>());
        EVENT_TO_HANDLER_MAP.put(event,this);
    }

    public abstract void register(String sn, BaseWebSocket webSocket, R param);

    public abstract void cancel(String sn);

    public abstract void trigger(T t);

}
