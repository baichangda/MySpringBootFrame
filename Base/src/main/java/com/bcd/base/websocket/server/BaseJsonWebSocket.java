package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseJsonWebSocket<T> extends BaseWebSocket {

    public final static ExecutorService WORK_POOL= Executors.newFixedThreadPool(16);

    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    protected JavaType paramJavaType;

    public BaseJsonWebSocket() {
        Type parentType= ClassUtil.getParentUntil(getClass(),BaseJsonWebSocket.class);
        this.paramJavaType=TypeFactory.defaultInstance().constructParametricType(WebSocketData.class, JsonUtil.getJavaType(((ParameterizedType)parentType).getActualTypeArguments()[0]));
    }

    @OnMessage
    public void onMessage(String jsonData, Session session){
        WORK_POOL.execute(()->{
            WebSocketData<JsonMessage> returnWebSocketData=new WebSocketData<>();
            JsonMessage jsonMessage;
            try {
                WebSocketData<T> paramWebSocketData = JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(jsonData, paramJavaType);
                logger.info("Receive WebSocket SN["+paramWebSocketData.getSn()+"]");
                returnWebSocketData.setSn(paramWebSocketData.getSn());
                jsonMessage=handle(paramWebSocketData.getData());
            } catch (Exception e) {
                logger.error("Error",e);
                jsonMessage= ExceptionUtil.toJsonMessage(e);
            }
            returnWebSocketData.setData(jsonMessage);
            logger.info("Send WebSocket SN["+returnWebSocketData.getSn()+"]");
            sendMessage(JsonUtil.toJson(returnWebSocketData));
        });

    }

    public abstract JsonMessage handle(T data) throws Exception;
}
