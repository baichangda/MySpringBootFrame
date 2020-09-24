package com.bcd.base.message;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.i18n.I18NData;

/**
 * Created by Administrator on 2017/7/26.
 */
public class ErrorMessage extends Message {
    public ErrorMessage(String msg) {
        super(msg);
    }

    public ErrorMessage(I18NData i18NData) {
        super(i18NData);
    }

    public JsonMessage toJsonMessage(Object... params) {
        return JsonMessage.fail().withMessage(getValue(params)).withCode(code);
    }

    public BaseRuntimeException toRuntimeException(Object... params) {
        return BaseRuntimeException.getException(getValue(params)).withCode(code);
    }

    public static ErrorMessage getMessage(String msg) {
        return new ErrorMessage(msg);
    }

    public static ErrorMessage getMessage(I18NData i18NData) {
        return new ErrorMessage(i18NData);
    }

    public ErrorMessage withCode(String code) {
        this.code = code;
        return this;
    }

    public ErrorMessage withMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ErrorMessage withI18NData(I18NData i18NData) {
        this.i18NData = i18NData;
        return this;
    }
}
