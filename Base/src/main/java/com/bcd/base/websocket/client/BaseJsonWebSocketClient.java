package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.client.data.WebSocketData;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class BaseJsonWebSocketClient<T> extends TextWebSocketHandler{

    public final Map<String,Consumer<WebSocketData<T>>> SN_TO_CALLBACK_MAP=new ConcurrentHashMap<>();

    /**
     * 0:session不可用
     * 1:正在初始化,session不可用
     * 2:初始化完成,session可用
     */
    AtomicInteger initStatus =new AtomicInteger(0);

    LinkedBlockingQueue<String> blockingMessageQueue=new LinkedBlockingQueue<>();

    protected Logger logger= LoggerFactory.getLogger(this.getClass());
    protected String url;
    protected JavaType javaType;

    protected WebSocketSession session;

    protected WebSocketConnectionManager manager;

    public WebSocketSession getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //1、更改状态为初始化中
        initStatus.set(1);
        //2、赋值session
        this.session=session;
        //3、更改状态为初始化完成
        initStatus.set(2);
        //4、进行阻塞数据发送
        String data;
        while ((data = blockingMessageQueue.poll()) != null) {
            sendMessage(data);
        }

    }

    public BaseJsonWebSocketClient(String url) {
        this.url=url;
        this.javaType= TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,
                JsonUtil.getJavaType(((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(throwable)->{
            logger.error("OpenConnection Failed,Will Open After 10 Seconds",throwable);
            try {
                Thread.sleep(10*1000L);
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
            manager.stop();
            manager.start();
        });
        manager.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            //1、转换结果集
            WebSocketData<T> result= JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(message.getPayload(),javaType);
            //2、触发onMessage方法
            onMessage(result);
        }catch (Exception ex){
            ExceptionUtil.printException(ex);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        initStatus.set(0);
        logger.warn("WebSocket Connection Closed,Will Restart It");
        this.session=null;
        this.manager.stop();
        this.manager.start();
    }

    public void sendMessage(String message){
        while(session==null||!session.isOpen()){
            int val= initStatus.get();
            switch (val){
                case 0:{
                    blockingMessageQueue.add(message);
                    logger.warn("RegisterWebSocketHandler Session Is Null Or Closed,Add Message To Queue");
                    break;
                }
                case 1:{
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                    break;
                }
                case 2:{
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                    break;
                }
                default:{
                    throw BaseRuntimeException.getException("Class["+this.getClass().getName()+"] initStatus["+val+"] Not Support");
                }
            }
        }
        TextMessage textMessage=new TextMessage(message);
        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public void onMessage(WebSocketData<T> result){
        //1、取出流水号
        Consumer<WebSocketData<T>> consumer= SN_TO_CALLBACK_MAP.get(result.getSn());
        //2、触发回调
        if(consumer!=null) {
            consumer.accept(result);
        }
    }
}
