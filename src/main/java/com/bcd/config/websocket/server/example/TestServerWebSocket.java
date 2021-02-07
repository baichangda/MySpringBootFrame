package com.bcd.config.websocket.server.example;

import com.bcd.base.websocket.server.BaseWebSocket;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/6/22.
 */
@Component
public class TestServerWebSocket extends BaseWebSocket {

    public TestServerWebSocket() {
        super("/ws/test");
    }

}
