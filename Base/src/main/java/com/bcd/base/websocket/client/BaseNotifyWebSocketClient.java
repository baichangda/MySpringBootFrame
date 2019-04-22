package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.bcd.base.websocket.data.nofity.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class BaseNotifyWebSocketClient extends BaseJsonWebSocketClient<NotifyCommand> {
    public final static Set<BaseNotifyWebSocketClient> ALL=new CopyOnWriteArraySet<>();

    public final ReentrantLock businessLock =new ReentrantLock();

    public final Map<String,NotifyInfo> sn_to_notify_info_map = new ConcurrentHashMap<>();

    public final ExecutorService reconnect_register_pool = Executors.newSingleThreadExecutor();

    protected boolean autoRegisterOnConnected;

    public BaseNotifyWebSocketClient(String url) {
        this(url,true);
    }

    public BaseNotifyWebSocketClient(String url, boolean autoRegisterOnConnected) {
        super(url);
        this.autoRegisterOnConnected=autoRegisterOnConnected;
    }

    /**
     * 注册监听
     * @param event
     * @param paramJson
     */
    public String register(NotifyEvent event, String paramJson, Consumer<String> consumer){
        businessLock.lock();
        try {
            String sn = RandomStringUtils.randomAlphanumeric(32);
            WebSocketData<JsonMessage<String>> webSocketData = register(sn,event,paramJson);
            if (webSocketData == null) {
                throw BaseRuntimeException.getException("Register Listener Timeout");
            } else {
                JsonMessage<String> jsonMessage = webSocketData.getData();
                if (jsonMessage.isResult()) {
                    sn_to_notify_info_map.put(sn, new NotifyInfo(sn,event, paramJson, consumer));
                    return sn;
                } else {
                    throw BaseRuntimeException.getException(jsonMessage.getMessage(), jsonMessage.getCode());
                }
            }
        }finally {
            businessLock.unlock();
        }
    }

    /**
     * 发送注册webSocket请求
     * @param sn
     * @param event
     * @param paramJson
     * @return
     */
    public WebSocketData<JsonMessage<String>> register(String sn, NotifyEvent event, String paramJson){
        return blockingRequest(new NotifyCommand(sn, NotifyCommandType.REGISTER, event, paramJson), 30 * 1000, JsonMessage.class, String.class);
    }

    /**
     * 取消监听
     * @param sn
     */
    public NotifyInfo cancel(String sn){
        businessLock.lock();
        try {
            if (!sn_to_notify_info_map.containsKey(sn)) {
                return null;
            }
            WebSocketData<JsonMessage<String>> webSocketData = blockingRequest(new NotifyCommand(sn, NotifyCommandType.CANCEL, null, null), 30 * 1000, JsonMessage.class, String.class);
            if (webSocketData == null) {
                throw BaseRuntimeException.getException("Cancel Listener Timeout");
            } else {
                JsonMessage<String> jsonMessage = webSocketData.getData();
                if (jsonMessage.isResult()) {
                    sn_to_notify_info_map.remove(sn);
                    return sn_to_notify_info_map.remove(sn);
                } else {
                    throw BaseRuntimeException.getException(jsonMessage.getMessage(), jsonMessage.getCode());
                }
            }
        }finally {
            businessLock.unlock();
        }
    }

    @Override
    public void onMessage(String data) {
        try {
            //1、转换结果集
            JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(data);
            //2、判断是命令或者通知数据
            if(jsonNode.hasNonNull("flag")){
                //2.1、如果是通知数据
                NotifyData notifyData= JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(data,NotifyData.class);
                logger.info("Receive Notify SN["+notifyData.getSn()+"] ");
                NotifyInfo notifyInfo= sn_to_notify_info_map.get(notifyData.getSn());
                notifyInfo.getConsumer().accept(notifyData.getDataJson());
            }else{
                //2.2、如果是命令
                String sn=jsonNode.get("sn").asText();
                logger.info("Receive WebSocket SN[" + sn + "]");
                //2.2.1、取出流水号
                Consumer<String> consumer = sn_to_callBack_map.remove(sn);
                //2.2.2、触发回调
                if (consumer == null) {
                    logger.warn("Receive No Consumer Message SN[" + sn + "]");
                } else {
                    consumer.accept(data);
                }
            }
        }catch (Exception e){
            ExceptionUtil.printException(e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        businessLock.lock();
        try {
            //1、如果启动了自动注册,则注册
            if (autoRegisterOnConnected) {
                reconnect_register_pool.execute(() -> {
                    Set<String> failedSnSet=new HashSet<>();
                    sn_to_notify_info_map.forEach((k,v)->{
                        WebSocketData<JsonMessage<String>> res=register(k,v.getEvent(), v.getParamJson());
                        if(res==null||!res.getData().isResult()){
                            failedSnSet.add(k);
                        }
                    });
                    //1.1、重新注册失败的则移除注册
                    failedSnSet.forEach(e->{
                        logger.error("Register["+e+"] After ReConnected Failed,Remove It");
                        sn_to_notify_info_map.remove(e);
                    });
                });
            }
        }finally {
            businessLock.unlock();
        }
    }
}
