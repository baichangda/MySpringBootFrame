package com.bcd.config.websocket.client.example;

import com.bcd.base.websocket.client.BaseTextWebSocketClient;
import org.springframework.beans.factory.annotation.Value;


//@Component
public class TestWebSocketClient extends BaseTextWebSocketClient {

    public TestWebSocketClient(@Value("${register.webSocket.url}") String url) {
        super(url);
    }

    @Override
    public void onMessage(String data) {
        logger.info("WebSocket Receive: " + data);
    }


}
