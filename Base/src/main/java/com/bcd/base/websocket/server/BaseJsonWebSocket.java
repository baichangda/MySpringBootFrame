package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseJsonWebSocket<T> extends BaseWebSocket {

    public final static ExecutorService WORK_POOL= Executors.newFixedThreadPool(16);

    protected JavaType paramJavaType;

    public BaseJsonWebSocket(String url) {
        super(url);
        Type parentType= ClassUtil.getParentUntil(getClass(),BaseJsonWebSocket.class);
        this.paramJavaType=TypeFactory.defaultInstance().constructParametricType(WebSocketData.class, JsonUtil.getJavaType(((ParameterizedType)parentType).getActualTypeArguments()[0]));
    }

    public void handleTextMessage(WebSocketSession session, TextMessage message){
        WORK_POOL.execute(()->{
            String data=message.getPayload();
            WebSocketData<JsonMessage> returnWebSocketData=new WebSocketData<>();
            JsonMessage jsonMessage;
            try {
                WebSocketData<T> paramWebSocketData = JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(data, paramJavaType);
                logger.info("Receive WebSocket SN["+paramWebSocketData.getSn()+"]");
                returnWebSocketData.setSn(paramWebSocketData.getSn());
                jsonMessage=handle(session_to_service_map.get(session),paramWebSocketData.getData());
            } catch (Exception e) {
                logger.error("Error",e);
                jsonMessage= ExceptionUtil.toJsonMessage(e);
            }
            returnWebSocketData.setData(jsonMessage);
            boolean sendRes= session_to_service_map.get(session).sendMessage(JsonUtil.toJson(returnWebSocketData));
            if(sendRes){
                logger.info("Send WebSocket SN["+returnWebSocketData.getSn()+"]");
            }else{
                logger.error("Send WebSocket SN["+returnWebSocketData.getSn()+"] Failed");
            }
        });

    }

    public abstract JsonMessage handle(ServiceInstance serviceInstance, T data) throws Exception;
}
