package com.bcd.base.websocket.client.data;

public class WebSocketData<T> {
    private String sn;
    private T data;

    public WebSocketData() {
    }

    public WebSocketData(String sn, T data) {
        this.sn = sn;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
