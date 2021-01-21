package com.bcd.base.message;

import com.bcd.base.i18n.I18NData;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/26.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String code;
    protected String msg;
    //对应的是message的I18NData
    protected I18NData i18NData;

    protected Message(String msg) {
        this.msg = msg;
    }

    protected Message(I18NData i18NData) {
        this.i18NData = i18NData;
    }

    public JsonMessage toJsonMessage(boolean result, Object... params) {
        return new JsonMessage(result).withMessage(getValue(params)).withCode(code);
    }

    /**
     * 存在两种情况信息展示:
     * 1、i18n数据模式
     *    此时params为i18n的占位符参数
     * 2、普通文本模式
     *    此时params为普通文本中的占位符参数、使用方式类似 {@link org.slf4j.Logger#info(String, Object...)}
     * @param params
     * @return
     */
    public String getValue(Object... params) {
        if (msg == null) {
            if (i18NData == null) {
                return null;
            } else {
                return i18NData.getValue(params);
            }
        } else {
            if(params==null||params.length==0){
                return msg;
            }else {
                return MessageFormatter.arrayFormat(msg, params, null).getMessage();
            }
        }
    }

    public static Message getMessage(String msg) {
        return new Message(msg);
    }

    public static Message getMessage(I18NData i18NData) {
        return new Message(i18NData);
    }

    public String getCode() {
        return code;
    }

    public Message withCode(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Message withMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public I18NData getI18NData() {
        return i18NData;
    }

    public Message withI18NData(I18NData i18NData) {
        this.i18NData = i18NData;
        return this;
    }
}
