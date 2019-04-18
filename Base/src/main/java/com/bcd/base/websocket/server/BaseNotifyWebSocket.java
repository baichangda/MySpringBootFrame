package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.nofity.NotifyCommand;
import com.bcd.base.websocket.data.nofity.NotifyData;
import com.bcd.base.websocket.data.nofity.NotifyHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.OnClose;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public abstract class BaseNotifyWebSocket extends BaseJsonWebSocket<NotifyCommand> {

    public BaseNotifyWebSocket(String url) {
        super(url);
    }

    @Override
    public JsonMessage handle(ServiceInstance serviceInstance, NotifyCommand data) throws Exception{
        NotifyHandler notifyHandler= NotifyHandler.EVENT_TO_HANDLER_MAP.get(data.getEvent());
        if(notifyHandler==null){
            throw BaseRuntimeException.getException("Event["+data.getEvent()+"] Has No Handler");
        }
        switch (data.getType()){
            case REGISTER:{
                if(notifyHandler.getRegisterParamJavaType().isTypeOrSubTypeOf(String.class)){
                    notifyHandler.register(data.getSn(),serviceInstance,data.getParamJson());
                }else{
                    Object param= data.getParamJson()==null?null: JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(data.getParamJson(), notifyHandler.getRegisterParamJavaType());
                    notifyHandler.register(data.getSn(),serviceInstance,param);
                }
                break;
            }
            case CANCEL:{
                notifyHandler.cancel(data.getSn());
                break;
            }
            default:{
                throw BaseRuntimeException.getException("Command Type["+data.getType()+"] Not Support");
            }
        }
        return JsonMessage.success();
    }

    @OnClose
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
        super.afterConnectionClosed(session,closeStatus);
        //断开连接后,清空掉此无效连接的监听
        NotifyHandler.EVENT_TO_SN_NOTIFY_MESSAGE_MAP.forEach((k1, v1)->{
            Set<String> removeSnSet=new HashSet<>();
            v1.forEach((k2,v2)->{
                if(v2.getServiceInstance().session==session)removeSnSet.add(k2);
            });
            removeSnSet.forEach(sn-> v1.remove(sn));
        });
    }


}
