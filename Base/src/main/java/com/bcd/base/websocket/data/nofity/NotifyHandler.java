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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 通知处理器
 * @param <T> 服务器通知客户端的数据类型
 * @param <R> 客户端注册请求参数类型
 */
public abstract class NotifyHandler<T,R> {

    protected Logger logger= LoggerFactory.getLogger(getClass());

    /**
     * 记录事件和handler对应关系
     */
    public final static Map<NotifyEvent,NotifyHandler> EVENT_TO_HANDLER_MAP=new HashMap<>();

    /**
     * 记录流水号和注册信息对应关系
     */
    protected final Map<String,RegisterInfo> sn_to_register_info_map=new ConcurrentHashMap<>();

    protected NotifyEvent event;

    protected JavaType registerParamJavaType;

    public NotifyEvent getEvent() {
        return event;
    }

    public JavaType getRegisterParamJavaType() {
        return registerParamJavaType;
    }

    public Map<String, RegisterInfo> getSn_to_register_info_map() {
        return sn_to_register_info_map;
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
     * @param serviceInstance 监听的客户端连接实体
     * @param param 注册参数
     */
    public void register(String sn, BaseWebSocket.ServiceInstance serviceInstance, R param){
        sn_to_register_info_map.put(sn,new RegisterInfo(sn,event,serviceInstance));
    }

    /**
     * 取消一个监听
     * @param sn 监听流水号
     */
    public void cancel(String sn){
        sn_to_register_info_map.remove(sn);
    }

    /**
     * 触发推送监听信息
     * @param t 信息
     */
    public abstract void trigger(T t);

    /**
     * 处理命令请求
     * @param serviceInstance
     * @param notifyCommand
     * @throws Exception
     */
    public static void handle(BaseWebSocket.ServiceInstance serviceInstance,NotifyCommand notifyCommand) throws Exception{
        NotifyHandler notifyHandler= NotifyHandler.EVENT_TO_HANDLER_MAP.get(notifyCommand.getEvent());
        if(notifyHandler==null){
            throw BaseRuntimeException.getException("Event["+notifyCommand.getEvent()+"] Has No Handler");
        }
        switch (notifyCommand.getType()){
            case REGISTER:{
                if(notifyHandler.getRegisterParamJavaType().isTypeOrSubTypeOf(String.class)){
                    notifyHandler.register(notifyCommand.getSn(),serviceInstance,notifyCommand.getParamJson());
                }else{
                    Object param= notifyCommand.getParamJson()==null?null: JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(notifyCommand.getParamJson(), notifyHandler.getRegisterParamJavaType());
                    notifyHandler.register(notifyCommand.getSn(),serviceInstance,param);
                }
                break;
            }
            case CANCEL:{
                notifyHandler.cancel(notifyCommand.getSn());
                break;
            }
            default:{
                throw BaseRuntimeException.getException("Command Type["+notifyCommand.getType()+"] Not Support");
            }
        }
    }

    /**
     * 取消某个连接上的所有监听信息
     * @param serviceInstance
     */
    public static void cancel(BaseWebSocket.ServiceInstance serviceInstance){
        EVENT_TO_HANDLER_MAP.values().forEach(e1->{
            e1.sn_to_register_info_map.values().stream().filter(e2->((RegisterInfo)e2).getServiceInstance()==serviceInstance).forEach(e2->{
                e1.cancel(((RegisterInfo)e2).getSn());
            });
        });
    }

}
