package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseWebSocket extends TextWebSocketHandler{

    protected ConcurrentHashMap<WebSocketSession,ServiceInstance> session_to_service_map=new ConcurrentHashMap<>();


    public static class ServiceInstance {
        public WebSocketSession session;

        public volatile long lastMessageTs=0L;

        public Long pingIntervalMills;

        public Long maxDisConnectMills;

        public boolean supportsPartialMessages;

        public ScheduledExecutorService heartBeatWorker;

        public ServiceInstance(WebSocketSession session,boolean supportsPartialMessages) {
            this.session = session;
            this.supportsPartialMessages=supportsPartialMessages;
            startHeartBeat(10 * 1000, 30 * 1000);
        }

        public void startHeartBeat(long pingIntervalMills,long maxDisConnectMills){
            this.pingIntervalMills=pingIntervalMills;
            this.maxDisConnectMills=maxDisConnectMills;
        }

        public void updateLastMessageTs(){
            if(maxDisConnectMills!=null){
                lastMessageTs=System.currentTimeMillis();
            }
        }

        public void doOnConnect(){
            //开始心跳定时任务
            if(maxDisConnectMills!=null) {
                heartBeatWorker =Executors.newSingleThreadScheduledExecutor();
                //开启定时发送ping
                heartBeatWorker.scheduleWithFixedDelay(()->{
                    try {
                        session.sendMessage(new PingMessage());
                    } catch (IOException e) {
                        ExceptionUtil.printException(e);
                    }
                },1000L,pingIntervalMills,TimeUnit.MILLISECONDS);
                //开启定时检查pong
                heartBeatWorker.scheduleWithFixedDelay(() -> {
                    if (System.currentTimeMillis() - lastMessageTs > maxDisConnectMills) {
                        try {
                            session.close();
                        } catch (IOException e) {
                            ExceptionUtil.printException(e);
                        }
                    }
                }, 1000L, maxDisConnectMills / 2, TimeUnit.MILLISECONDS);
            }
        }

        public void doOnDisConnect(){
            //终止心跳定时任务
            if(this.maxDisConnectMills!=null) {
                if(this.heartBeatWorker !=null){
                    this.heartBeatWorker.shutdown();
                    this.heartBeatWorker =null;
                }
            }
        }

        /**
         * 发送消息
         * @param message
         * @throws IOException
         */
        public boolean sendMessage(String message){
            try {
                if(session==null||!session.isOpen()){
                    return false;
                }else {
                    List<String> subList;
                    if(supportsPartialMessages) {
                        subList = new LinkedList<>();
                        int len = message.length();
                        int index = 0;
                        while (true) {
                            int start = index;
                            int end = index + 1024;
                            if (end >= len) {
                                break;
                            }
                            String sub = message.substring(start, end);
                            subList.add(sub);
                            index = end;
                        }
                        subList.add(message.substring(index, len));
                    }else{
                        subList= Arrays.asList(message);
                    }
                    synchronized (this) {
                        if(session==null||!session.isOpen()){
                            return false;
                        }else {
                            int subSize=subList.size();
                            for(int i=0;i<=subSize-2;i++){
                                this.session.sendMessage(new TextMessage(subList.get(i),false));
                            }
                            this.session.sendMessage(new TextMessage(subList.get(subSize-1),true));
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    protected String url;

    protected Logger logger= LoggerFactory.getLogger(getClass());

    public BaseWebSocket(String url) {
        this.url = url;

    }

    public String getUrl() {
        return url;
    }

    /**
     * 连接打开时候触发
     * @param session
     */
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        ServiceInstance serviceInstance= new ServiceInstance(session,supportsPartialMessages());
        session_to_service_map.put(session,serviceInstance);
        serviceInstance.updateLastMessageTs();

    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception{
        session_to_service_map.get(session).updateLastMessageTs();
        super.handleMessage(session,message);
    }


    /**
     * 连接关闭时候触发
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
        ServiceInstance serviceInstance=session_to_service_map.remove(session);
        serviceInstance.doOnDisConnect();
    }

    /**
     * 发生错误时候触发
     * @param session
     * @param exception
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception{
        ExceptionUtil.printException(exception);
    }




    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
