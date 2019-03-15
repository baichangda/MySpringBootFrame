package com.bcd.base.websocket.client.impl;

import com.bcd.base.websocket.BaseTextWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


//@Component
public class RegisterWebSocketHandler extends BaseTextWebSocketClient {

    Logger logger= LoggerFactory.getLogger(RegisterWebSocketHandler.class);

    public RegisterWebSocketHandler(@Value("${register.webSocket.url}") String url) {
        super(url);
    }

    @Override
    public void onMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("WebSocket Receive: "+message.getPayload());
        super.handleTextMessage(session, message);
    }


}
