package com.bcd.base.websocket.data.nofity;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.server.BaseWebSocket;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

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
@SuppressWarnings("unchecked")
public abstract class NotifyHandler<T,R> {

    static Logger staticLogger= LoggerFactory.getLogger(NotifyHandler.class);

    protected Logger logger= LoggerFactory.getLogger(getClass());

    /**
     * 记录事件和handler对应关系
     */
    public final static Map<NotifyEvent,NotifyHandler> EVENT_TO_HANDLER_MAP=new HashMap<>();

    /**
     * 记录流水号和注册信息对应关系
     */
    protected final Map<String,NotifyChannel> snToNotifyChannel =new ConcurrentHashMap<>();

    protected NotifyEvent event;

    protected JavaType registerParamJavaType;

    public NotifyEvent getEvent() {
        return event;
    }

    public JavaType getRegisterParamJavaType() {
        return registerParamJavaType;
    }

    public Map<String, NotifyChannel> getSnToNotifyChannel() {
        return snToNotifyChannel;
    }

    public NotifyHandler(NotifyEvent event) {
        this.event = event;
        Type parentType= ClassUtil.getParentUntil(getClass(),NotifyHandler.class);
        this.registerParamJavaType= JsonUtil.getJavaType(((ParameterizedType)parentType).getActualTypeArguments()[1]);
        EVENT_TO_HANDLER_MAP.put(event,this);
    }

    /**
     * 注册一个监听
     * @param sn 监听流水号
     * @param session 监听的客户端连接实体
     * @param param 注册参数
     */
    public NotifyChannel register(String sn, BaseWebSocket webSocket, WebSocketSession session, R param){
        NotifyChannel notifyChannel=new NotifyChannel(sn,event,webSocket,session);
        snToNotifyChannel.put(sn,notifyChannel);
        return notifyChannel;
    }

    /**
     * 取消一个监听
     * @param sn 监听流水号
     */
    public NotifyChannel cancel(String sn){
        return snToNotifyChannel.remove(sn);
    }

    /**
     * 触发推送监听信息
     * @param t 信息
     */
    public abstract void trigger(T t);

    /**
     * 处理命令请求
     * @param webSocket
     * @param session
     * @param notifyCommand
     * @throws Exception
     */
    public static void handle(BaseWebSocket webSocket, WebSocketSession session, NotifyCommand notifyCommand) throws Exception{
        NotifyHandler notifyHandler= NotifyHandler.EVENT_TO_HANDLER_MAP.get(notifyCommand.getEvent());
        if(notifyHandler==null){
            throw BaseRuntimeException.getException("Event["+notifyCommand.getEvent()+"] Has No Handler");
        }
        staticLogger.debug("Receive Command["+notifyCommand.getEvent()+"] Type["+notifyCommand.getType()+"] From["+session.getRemoteAddress().toString()+"] On ["+webSocket.getUrl()+"]:\n"+JsonUtil.toJson(notifyCommand));
        switch (notifyCommand.getType()){
            case REGISTER:{
                if(notifyHandler.getRegisterParamJavaType().isTypeOrSubTypeOf(String.class)){
                    notifyHandler.register(notifyCommand.getSn(),webSocket,session,notifyCommand.getParamJson());
                }else{
                    Object param= notifyCommand.getParamJson()==null?null: JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(notifyCommand.getParamJson(), notifyHandler.getRegisterParamJavaType());
                    notifyHandler.register(notifyCommand.getSn(),webSocket,session,param);
                }
                break;
            }
            case CANCEL:{
                notifyHandler.cancel(notifyCommand.getSn());
                break;
            }
            default:{
                throw BaseRuntimeException.getException("Command Event["+notifyCommand.getEvent()+"] Type["+notifyCommand.getType()+"] Not Support");
            }
        }
    }

    /**
     * 取消某个连接上的所有监听信息
     * @param session
     */
    public static void cancel(WebSocketSession session){
        EVENT_TO_HANDLER_MAP.values().forEach(e1->{
            e1.snToNotifyChannel.values().stream().filter(e2->((NotifyChannel)e2).getSession()==session).forEach(e2->{
                staticLogger.warn("Cancel Notify Listener SN["+((NotifyChannel)e2).getSn()+"] On "+session.getRemoteAddress()+" AfterConnectionClosed");
                e1.cancel(((NotifyChannel)e2).getSn());
            });
        });
    }

}
