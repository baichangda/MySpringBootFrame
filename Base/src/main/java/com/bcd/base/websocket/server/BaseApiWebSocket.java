package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.websocket.data.api.ApiCommand;
import com.bcd.base.websocket.data.api.ApiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseApiWebSocket extends BaseJsonWebSocket<ApiCommand> {
    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public JsonMessage handle(ApiCommand data) throws Exception{
        ApiHandler apiHandler= ApiHandler.NAME_TO_HANDLER_MAP.get(data.getApiName());
        return apiHandler.execute(data.getParamJson());
    }
}
