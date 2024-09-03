package com.bcd.base.support_spring_websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    static Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    List<PathTextWebSocketHandler> handlers;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (PathTextWebSocketHandler handler : handlers) {
            registry.addHandler(handler, handler.paths).setAllowedOrigins("*");
            logger.info("register websocket paths[{}] handler[{}]",
                    String.join(",", handler.paths),
                    handler.getClass());
        }
    }
}
