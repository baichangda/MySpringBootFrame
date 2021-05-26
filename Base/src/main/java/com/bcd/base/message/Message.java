package com.bcd.base.message;

import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/26.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String code;
    protected String msg;

    protected Message(String msg) {
        this.msg = msg;
    }

    public static Message getMessage(String msg) {
        return new Message(msg);
    }

    public JsonMessage<?> toJsonMessage(boolean result, Object... params) {
        return new JsonMessage<>(result,null).message(getValue(params)).code(code);
    }

    /**
     * 存在两种情况信息展示:
     * 1、i18n数据模式
     * 此时params为i18n的占位符参数
     * 2、普通文本模式
     * 此时params为普通文本中的占位符参数、使用方式类似 {@link org.slf4j.Logger#info(String, Object...)}
     *
     * @param params
     * @return
     */
    public String getValue(Object... params) {
        if (params == null || params.length == 0) {
            return msg;
        } else {
            return MessageFormatter.arrayFormat(msg, params, null).getMessage();
        }
    }

    public String getCode() {
        return code;
    }

    public Message code(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Message msg(String msg) {
        this.msg = msg;
        return this;
    }
}
