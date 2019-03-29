package com.bcd.base.websocket.data.nofity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotifyCommandType {
    REGISTER(1),
    CANCEL(2);

    int flag;

    @JsonValue
    public int getFlag() {
        return flag;
    }

    @JsonCreator
    public static NotifyCommandType toCommandType(int flag) {
        for(NotifyCommandType e:values()){
            if(e.getFlag()==flag){
                return e;
            }
        }
        return null;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    NotifyCommandType(int flag){
        this.flag=flag;
    }
}