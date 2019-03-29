package com.bcd.base.websocket.data.nofity;


public class NotifyCommand {
    //注册监听流水号
    private String sn;
    //请求类型(1:注册监听;2:取消监听)
    private NotifyCommandType type;
    //监听事件
    private NotifyEvent event;
    //附加参数
    private String paramJson;

    public NotifyCommand() {
    }

    public NotifyCommand(String sn, NotifyCommandType type, NotifyEvent event, String paramJson) {
        this.sn = sn;
        this.type = type;
        this.event = event;
        this.paramJson = paramJson;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public NotifyCommandType getType() {
        return type;
    }

    public void setType(NotifyCommandType type) {
        this.type = type;
    }

    public NotifyEvent getEvent() {
        return event;
    }

    public void setEvent(NotifyEvent event) {
        this.event = event;
    }

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }
}
