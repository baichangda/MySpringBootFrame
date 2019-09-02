package com.bcd.base.websocket.data.nofity;

import com.bcd.base.util.JsonUtil;
import org.springframework.web.socket.WebSocketSession;

@SuppressWarnings("unchecked")
public class CommonNotifyHandler<T> extends NotifyHandler<T, String> {

    public CommonNotifyHandler(NotifyEvent event) {
        super(event);
    }

    @Override
    public void trigger(T data) {
        snToNotifyChannel.forEach((k, v) -> {
            NotifyData sendData = packData(k, data);
            logger.info("Send Notify SN[" + k + "] Event[" + event + "]");
            v.sendMessage(JsonUtil.toJson(sendData));
        });
    }

    protected NotifyData packData(String notifySn, T data) {
        NotifyData notifyData = new NotifyData();
        notifyData.setSn(notifySn);
        notifyData.setDataJson(JsonUtil.toJson(data));
        return notifyData;
    }
}
