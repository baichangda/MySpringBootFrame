package com.bcd.base.websocket.data.nofity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotifyEvent {
    //车辆信息监听事件
    VEHICLE_ON_ADD(11),
    VEHICLE_ON_UPDATE(12),
    VEHICLE_ON_DELETE(13),

    //车型信息监听事件
    VEHICLE_TYPE_ON_ADD(21),
    VEHICLE_TYPE_ON_UPDATE(22),
    VEHICLE_TYPE_ON_DELETE(23),

    //车型信号监听事件
    VEHICLE_TYPE_SIGNAL_ON_ADD(31),
    VEHICLE_TYPE_SIGNAL_ON_UPDATE(32),
    VEHICLE_TYPE_SIGNAL_ON_DELETE(33);

    int flag;

    @JsonValue
    public int getFlag() {
        return flag;
    }

    @JsonCreator
    public static NotifyEvent toEvent(int flag) {
        for(NotifyEvent e:values()){
            if(e.getFlag()==flag){
                return e;
            }
        }
        return null;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    NotifyEvent(int flag){
        this.flag=flag;
    }
}