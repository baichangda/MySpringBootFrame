package com.bcd.base.websocket.client.impl;

import com.bcd.base.websocket.client.BaseTextWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


//@Component
public class TestWebSocketClient extends BaseTextWebSocketClient {

    public TestWebSocketClient(@Value("${register.webSocket.url}") String url) {
        super(url);
    }

    @Override
    public void onMessage(WebSocketSession session, String data) throws Exception {
        logger.info("WebSocket Receive: "+data);
    }


}
