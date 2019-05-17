package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.websocket.data.api.ApiCommand;
import com.bcd.base.websocket.data.api.ApiHandler;
import org.springframework.web.socket.WebSocketSession;

public abstract class BaseApiWebSocket extends BaseJsonWebSocket<ApiCommand> {
    public BaseApiWebSocket(String url) {
        super(url);
    }

    @Override
    public JsonMessage handle(WebSocketSession session, ApiCommand data) throws Exception{
        ApiHandler apiHandler= ApiHandler.NAME_TO_HANDLER_MAP.get(data.getApiName());
        return apiHandler.execute(data.getParamJson());
    }
}
