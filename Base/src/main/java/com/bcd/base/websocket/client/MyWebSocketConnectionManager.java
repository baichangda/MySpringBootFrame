package com.bcd.base.websocket.client;

import org.springframework.lang.Nullable;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.util.function.Consumer;

public class MyWebSocketConnectionManager extends WebSocketConnectionManager {
    private final WebSocketClient client;

    private final WebSocketHandler webSocketHandler;

    private final Consumer<Throwable> onOpenFailureCallBack;

    private final Consumer<WebSocketSession> onOpenSuccessCallBack;

    @Nullable
    private WebSocketSession webSocketSession;

    private WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public MyWebSocketConnectionManager(WebSocketClient client, WebSocketHandler webSocketHandler, String uriTemplate, Consumer<WebSocketSession> onOpenSuccessCallBack, Consumer<Throwable> onOpenFailureCallBack, Object... uriVariables) {
        super(client, webSocketHandler, uriTemplate, uriVariables);
        this.client = client;
        this.onOpenSuccessCallBack = onOpenSuccessCallBack;
        this.onOpenFailureCallBack = onOpenFailureCallBack;
        this.webSocketHandler = decorateWebSocketHandler(webSocketHandler);
    }

    @Override
    protected void openConnection() {
        if (logger.isInfoEnabled()) {
            logger.info("Connecting to WebSocket at " + getUri());
        }

        ListenableFuture<WebSocketSession> future =
                this.client.doHandshake(this.webSocketHandler, this.headers, getUri());

        future.addCallback(new ListenableFutureCallback<WebSocketSession>() {
            @Override
            public void onSuccess(@Nullable WebSocketSession result) {
                webSocketSession = result;
                onOpenSuccessCallBack.accept(result);
            }

            @Override
            public void onFailure(Throwable ex) {
                onOpenFailureCallBack.accept(ex);
            }
        });
    }
}
