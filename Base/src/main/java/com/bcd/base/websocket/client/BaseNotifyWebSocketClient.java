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

    protected boolean autoRegisterOnConnected;

    public BaseNotifyWebSocketClient(String url) {
        this(url,true);
    }

    public BaseNotifyWebSocketClient(String url,boolean autoRegisterOnConnected) {
        super(url);
        this.autoRegisterOnConnected=autoRegisterOnConnected;
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
    public void onMessage(String data) {
        try {
            //1、转换结果集
            JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(data);
            //2、判断是命令或者通知数据
            if(jsonNode.hasNonNull("sn")){
                //2.1、如果是命令
                String sn=jsonNode.get("sn").asText();
                logger.info("Receive WebSocket SN[" + sn + "]");
                //2.1.1、取出流水号
                Consumer<String> consumer = sn_to_callBack_map.remove(sn);
                //2.1.2、触发回调
                if (consumer == null) {
                    logger.warn("Receive No Consumer Message SN[" + sn + "]");
                } else {
                    consumer.accept(data);
                }
            }else{
                //2.2、如果是通知数据
                NotifyData notifyData= JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(data,NotifyData.class);
                logger.info("Receive Notify SN["+notifyData.getSn()+"] ");
                NotifyConsumer notifyConsumer= sn_to_consumer.get(notifyData.getSn());
                notifyConsumer.getConsumer().accept(notifyData.getDataJson());
            }
        }catch (Exception e){
            ExceptionUtil.printException(e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //1、断线重连之后,检测之前是否存在监听,有则重新注册所有监听
        List<NotifyConsumer> notifyConsumerList=new ArrayList<>(sn_to_consumer.values());
        //2、获取所有的监听流水号并清空监听信息
        Set<String> snSet=new HashSet<>(sn_to_consumer.keySet());
        sn_to_consumer.clear();
        snSet.forEach(sn-> SN_TO_NOTIFY_INFO_MAP.remove(sn));
        //3、如果启动了自动注册,则注册
        if(autoRegisterOnConnected){
            if(!notifyConsumerList.isEmpty()) {
                RECONNECT_REGISTER_POOL.execute(() -> {
                    notifyConsumerList.forEach(e -> register(e.getEvent(), e.getParamJson(), e.getConsumer()));
                });
            }
        }

    }
}
