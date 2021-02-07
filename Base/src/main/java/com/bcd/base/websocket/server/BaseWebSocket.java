package com.bcd.base.websocket.server;

import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class BaseWebSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    protected final Map<WebSocketSession, ExecutorService> clientSessionToPool = new ConcurrentHashMap<>();

    protected final Map<WebSocketSession, Long> clientSessionToLastPong = new ConcurrentHashMap<>();

    protected String url;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected long timeOutPongTimeInMillis = 60 * 1000;
    protected ScheduledExecutorService timeOutPongPool;

    public BaseWebSocket(String url) {
        this.url = url;
        startHeartBeatCheck();
    }

    public String getUrl() {
        return url;
    }


    /**
     * 开启心跳检测
     * 处理如下情况:
     * 1、由于长时间客户端和服务端不进行数据通信,此时由于防火墙等其他因素关闭tcp连接,导致不可用
     * 此时服务器应该关闭session同时释放资源
     * <p>
     * 2、tcp协议层下的硬件层网络连接突然断开,此时在tcp层面此连接依然可用,此时需要断开连接;
     * 此时close源码参考:
     * {@link org.apache.tomcat.websocket.WsSession#sendCloseMessage(CloseReason)}
     * 1、close源码在此情况tomcat源码先会调用发送close message到客户端直到超时,关闭远程session
     * 2、检测是否{@link javax.websocket.CloseReason.CloseCodes#CLOSED_ABNORMALLY},
     * 对应{@link CloseStatus#NO_CLOSE_FRAME},是则触发onError
     * 3、触发onClose
     */
    public void startHeartBeatCheck() {
        timeOutPongPool = Executors.newSingleThreadScheduledExecutor();
        timeOutPongPool.scheduleWithFixedDelay(() -> {
            clientSessionToLastPong.forEach((k, v) -> {
                if ((System.currentTimeMillis() - v) > timeOutPongTimeInMillis) {
                    try {
                        k.close(CloseStatus.NO_CLOSE_FRAME);
                    } catch (Exception e) {
                        logger.error("close session after pong timeout error", e);
                    }
                }
            });
        }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 发送信息
     *
     * @param session
     * @param message
     */
    public void sendMessage(WebSocketSession session, String message) {
        clientSessionToPool.get(session).execute(() -> {
            try {
                if (session != null && session.isOpen()) {
                    List<String> subList;
                    if (supportsPartialMessages()) {
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
                    } else {
                        subList = Arrays.asList(message);
                    }
                    synchronized (session) {
                        if (session.isOpen()) {
                            int subSize = subList.size();
                            for (int i = 0; i <= subSize - 2; i++) {
                                session.sendMessage(new TextMessage(subList.get(i), false));
                            }
                            session.sendMessage(new TextMessage(subList.get(subSize - 1), true));
                        } else {
                            logger.error("Session Has Been Closed");
                        }
                    }
                } else {
                    logger.error("Session Has Been Closed");
                }
            } catch (IOException e) {
                try {
                    //发送信息失败则关闭session
                    logger.error("send message error", e);
                    session.close(CloseStatus.NO_CLOSE_FRAME);
                } catch (IOException ex) {
                    logger.error("close session after send message error", e);
                }
            }
        });
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this, url).setAllowedOrigins("*");
    }

    /**
     * 连接打开时候触发
     *
     * @param session
     */
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("session[" + session.getRemoteAddress().toString() + "] afterConnectionEstablished");
        clientSessionToPool.put(session, Executors.newSingleThreadExecutor());
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
        clientSessionToLastPong.put(session, System.currentTimeMillis());
    }

    /**
     * 连接关闭时候触发
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.debug("session[" + session.getRemoteAddress().toString() + "] afterConnectionClosed,Reason[" + closeStatus + "]");
        ExecutorService pool = clientSessionToPool.remove(session);
        clientSessionToLastPong.remove(session);
        if (pool != null) {
            pool.shutdown();
        }
    }

    /**
     * 发生错误时候触发
     *
     * @param session
     * @param exception
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        ExceptionUtil.printException(exception);
    }


    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
