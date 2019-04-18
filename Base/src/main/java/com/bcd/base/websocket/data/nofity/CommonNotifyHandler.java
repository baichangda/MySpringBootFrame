package com.bcd.base.websocket.data.nofity;

import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.server.BaseWebSocket;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class CommonNotifyHandler<T> extends NotifyHandler<T,String> {
    protected ConcurrentHashMap<String,NotifyMessage> sn_to_notify_message_map=new ConcurrentHashMap<>();

    public CommonNotifyHandler(NotifyEvent event) {
        super(event);
    }

    @Override
    public void register(String sn, BaseWebSocket.ServiceInstance serviceInstance, String param) {
        sn_to_notify_message_map.put(sn,new NotifyMessage(sn,event,serviceInstance));
    }

    @Override
    public void cancel(String sn) {
        sn_to_notify_message_map.remove(sn);
    }

    @Override
    public void trigger(T data) {
        sn_to_notify_message_map.forEach((k,v)->{
            BaseWebSocket.ServiceInstance serviceInstance= v.getServiceInstance();
            NotifyData sendData=packData(k,data);
            logger.info("Send Notify SN["+k+"] Event["+event+"]");
            serviceInstance.sendMessage(JsonUtil.toJson(sendData));
        });
    }

    protected NotifyData packData(String notifySn, T data){
        NotifyData notifyData= new NotifyData();
        notifyData.setSn(notifySn);
        notifyData.setDataJson(JsonUtil.toJson(data));
        return notifyData;
    }
}
