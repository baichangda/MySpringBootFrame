package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.nofity.NotifyCommand;
import com.bcd.base.websocket.data.nofity.NotifyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseNotifyWebSocket extends BaseJsonWebSocket<NotifyCommand>{

    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public JsonMessage handle(NotifyCommand data) throws Exception{
        NotifyHandler notifyHandler= NotifyHandler.EVENT_TO_HANDLER_MAP.get(data.getEvent());
        if(notifyHandler==null){
            throw BaseRuntimeException.getException("Event["+data.getEvent()+"] Has No Handler");
        }
        switch (data.getType()){
            case REGISTER:{
                Object param= data.getParamJson()==null?null: JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(data.getParamJson(), notifyHandler.getRegisterParamJavaType());
                notifyHandler.register(data.getSn(),this,param);
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
    public void onClose(Session session, CloseReason closeReason){
        super.onClose(session,closeReason);
        //断开连接后,清空掉此无效连接的监听
        NotifyHandler.EVENT_TO_SN_NOTIFY_MESSAGE_MAP.forEach((k1, v1)->{
            Set<String> removeSnSet=new HashSet<>();
            v1.forEach((k2,v2)->{
                if(v2.getWebSocket()==this)removeSnSet.add(k2);
            });
            removeSnSet.forEach(sn-> v1.remove(sn));
        });
    }
}
