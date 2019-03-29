package com.bcd.base.websocket.client;


import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.bcd.base.websocket.data.api.ApiCommand;

public abstract class BaseApiWebSocketClient extends BaseJsonWebSocketClient<ApiCommand> {
    public BaseApiWebSocketClient(String url) {
        super(url);
    }

    /**
     *  调用方法
     */
    public <R>WebSocketData<R> call(String apiName, Object[] params, long timeOut, Class ... clazzs){
        ApiCommand apiCommand=new ApiCommand();
        apiCommand.setApiName(apiName);
        apiCommand.setParamJson(JsonUtil.toJson(params));
        return blockingRequest(apiCommand,timeOut,clazzs);
    }
}
