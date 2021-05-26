package com.bcd.base.message;

import com.bcd.base.exception.BaseRuntimeException;

/**
 * Created by Administrator on 2017/7/26.
 */
public class ErrorMessage extends Message {
    public ErrorMessage(String msg) {
        super(msg);
    }

    public static ErrorMessage getMessage(String msg) {
        return new ErrorMessage(msg);
    }

    public JsonMessage toJsonMessage(Object... params) {
        return JsonMessage.fail().message(getValue(params)).code(code);
    }

    public BaseRuntimeException toRuntimeException(Object... params) {
        return BaseRuntimeException.getException(getValue(params)).code(code);
    }

    public ErrorMessage code(String code) {
        this.code = code;
        return this;
    }

    public ErrorMessage msg(String msg) {
        this.msg = msg;
        return this;
    }
}
