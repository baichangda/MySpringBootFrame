package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.bcd.base.websocket.data.nofity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class BaseNotifyWebSocketClient extends BaseJsonWebSocketClient<NotifyCommand> {
    public final Map<String,NotifyConsumer> sn_to_consumer =new ConcurrentHashMap<>();

    public final static Map<String,NotifyInfo> SN_TO_NOTIFY_INFO_MAP = new ConcurrentHashMap<>();

    public final static ExecutorService RECONNECT_REGISTER_POOL= Executors.newSingleThreadExecutor();

    public BaseNotifyWebSocketClient(String url) {
        super(url);
    }

    /**
     * 注册监听
     * @param event
     * @param paramJson
     */
    public String register(NotifyEvent event, String paramJson, Consumer<String> consumer){
        String sn= RandomStringUtils.randomAlphanumeric(32);
        WebSocketData<JsonMessage<String>> webSocketData= blockingRequest(new NotifyCommand(sn, NotifyCommandType.REGISTER,event,paramJson),30*1000, JsonMessage.class,String.class);
        if(webSocketData==null){
            throw BaseRuntimeException.getException("Register Listener Timeout");
        }else{
            JsonMessage<String> jsonMessage= webSocketData.getData();
            if(jsonMessage.isResult()){
                sn_to_consumer.put(sn,new NotifyConsumer(event,paramJson, consumer));
                SN_TO_NOTIFY_INFO_MAP.putIfAbsent(sn,new NotifyInfo(sn,event));
                return sn;
            }else {
                throw BaseRuntimeException.getException(jsonMessage.getMessage(), jsonMessage.getCode());
            }
        }
    }

    /**
     * 取消监听
     * @param sn
     * @param event
     */
    public void cancel(String sn,NotifyEvent event){
        WebSocketData<JsonMessage<String>> webSocketData= blockingRequest(new NotifyCommand(sn, NotifyCommandType.CANCEL,event,null),30*1000, JsonMessage.class,String.class);
        if(webSocketData==null){
            throw BaseRuntimeException.getException("Cancel Listener Timeout");
        }else{
            JsonMessage<String> jsonMessage= webSocketData.getData();
            if(jsonMessage.isResult()){
                sn_to_consumer.remove(sn);
                SN_TO_NOTIFY_INFO_MAP.remove(sn);
            }else{
                throw BaseRuntimeException.getException(jsonMessage.getMessage(),jsonMessage.getCode());
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            //1、转换结果集
            String jsonData=message.getPayload();
            JsonNode jsonNode= JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(jsonData);
            //2、判断是命令或者通知数据
            if(jsonNode.hasNonNull("sn")){
                //2.1、如果是命令,触发onMessage方法
                onMessage(jsonNode.get("sn").asText(),jsonData);
            }else{
                //2.2、如果是通知数据
                WebSocketData<NotifyData> webSocketData= JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(jsonData,TypeFactory.defaultInstance().constructParametricType(WebSocketData.class, NotifyData.class));
                logger.info("Receive Notify SN["+webSocketData.getData().getSn()+"] ");
                NotifyConsumer notifyConsumer= sn_to_consumer.get(webSocketData.getData().getSn());
                notifyConsumer.getConsumer().accept(webSocketData.getData().getDataJson());
            }
        }catch (Exception ex){
            ExceptionUtil.printException(ex);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //断线重连之后,检测之前是否存在监听,有则重新注册所有监听
        List<NotifyConsumer> notifyConsumerList= new ArrayList<>(sn_to_consumer.values());
        Set<String> snSet=new HashSet<>(sn_to_consumer.keySet());
        sn_to_consumer.clear();
        snSet.forEach(sn-> SN_TO_NOTIFY_INFO_MAP.remove(sn));
        if(!notifyConsumerList.isEmpty()) {
            RECONNECT_REGISTER_POOL.execute(() -> {
                notifyConsumerList.forEach(e -> register(e.getEvent(), e.getParamJson(), e.getConsumer()));
            });
        }
    }
}
