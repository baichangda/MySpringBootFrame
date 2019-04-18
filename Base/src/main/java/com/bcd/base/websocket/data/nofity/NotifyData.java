package com.bcd.base.websocket.data.nofity;


public class NotifyData {
    //用于转化为json时候和WebSocketData格式区分开
    private int flag=0;
    private String sn;
    private String dataJson;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }
}
