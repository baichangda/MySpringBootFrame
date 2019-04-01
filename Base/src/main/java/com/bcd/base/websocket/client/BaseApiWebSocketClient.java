package com.bcd.base.websocket.client;


import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.bcd.base.websocket.data.api.ApiCommand;

public abstract class BaseApiWebSocketClient extends BaseJsonWebSocketClient<ApiCommand> {
    public BaseApiWebSocketClient(String url) {
        super(url);
    }

    /**
     * 阻塞调用请求
     * @param apiName 需要调用的api名称
     * @param params  调用api需要传入的参数
     * @param timeOut  调用api的超时事件
     * @param clazzs 返回参数泛型类型数组,只支持类型单泛型,例如:
     *               JsonMessage<<WebData<VehicleBean>>> 传参数 JsonMessage.class,WebData.class,VehicleBean.class
     * @return 返回null表示超时;其他则表示正常
     */
    public <R>WebSocketData<R> call(String apiName, Object[] params, long timeOut, Class ... clazzs){
        ApiCommand apiCommand=new ApiCommand();
        apiCommand.setApiName(apiName);
        apiCommand.setParamJson(JsonUtil.toJson(params));
        return blockingRequest(apiCommand,timeOut,clazzs);
    }
}
