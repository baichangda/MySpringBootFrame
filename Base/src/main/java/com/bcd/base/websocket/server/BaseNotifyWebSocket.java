package com.bcd.base.websocket.server;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.websocket.data.nofity.NotifyCommand;
import com.bcd.base.websocket.data.nofity.NotifyHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.OnClose;

@SuppressWarnings("unchecked")
public abstract class BaseNotifyWebSocket extends BaseJsonWebSocket<NotifyCommand> {

    public BaseNotifyWebSocket(String url) {
        super(url);
    }

    @Override
    public JsonMessage handle(WebSocketSession session, NotifyCommand data) throws Exception {
        NotifyHandler.handle(this, session, data);
        return JsonMessage.success();
    }

    @OnClose
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        super.afterConnectionClosed(session, closeStatus);
        //断开连接后,清空掉此无效连接的监听
        NotifyHandler.cancel(session);
    }

}
