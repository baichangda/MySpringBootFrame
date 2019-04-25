package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.websocket.data.nofity.NotifyCommand;
import com.bcd.base.websocket.data.nofity.NotifyHandler;

import javax.websocket.OnClose;

@SuppressWarnings("unchecked")
public abstract class BaseNotifyWebSocket extends BaseJsonWebSocket<NotifyCommand> {

    public BaseNotifyWebSocket(String url) {
        super(url);
    }

    @Override
    public JsonMessage handle(ServiceInstance serviceInstance, NotifyCommand data) throws Exception{
        NotifyHandler.handle(serviceInstance,data);
        return JsonMessage.success();
    }

    @OnClose
    public void afterConnectionClosed(ServiceInstance serviceInstance){
        super.afterConnectionClosed(serviceInstance);
        //断开连接后,清空掉此无效连接的监听
        NotifyHandler.cancel(serviceInstance);
    }

}
