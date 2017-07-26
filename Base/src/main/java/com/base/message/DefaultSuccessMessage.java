package com.base.message;

import com.base.i18n.I18NData;
import com.base.json.JsonMessage;

/**
 * Created by Administrator on 2017/7/26.
 */
public class DefaultSuccessMessage extends BaseSuccessMessage{
    private String code;
    private String msg;
    //对应的是message的I18NData
    private I18NData i18NData;

    public DefaultSuccessMessage() {
    }

    public DefaultSuccessMessage(String msg) {
        this.msg = msg;
    }

    public DefaultSuccessMessage(I18NData i18NData) {
        this.i18NData = i18NData;
    }

    public DefaultSuccessMessage(String msg,String code) {
        this.code = code;
        this.msg = msg;
    }

    public DefaultSuccessMessage(I18NData i18NData,String code) {
        this.code = code;
        this.i18NData = i18NData;
    }


    @Override
    public JsonMessage toJsonMessage() {
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public I18NData getI18NData() {
        return i18NData;
    }

    public void setI18NData(I18NData i18NData) {
        this.i18NData = i18NData;
    }
}
