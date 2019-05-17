package com.bcd.config.websocket.server.example;

import com.bcd.base.websocket.server.BaseWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Administrator on 2017/6/22.
 */
@Component
public class TestServerWebSocket extends BaseWebSocket{

    public TestServerWebSocket() {
        super("/ws/test");
    }

}
