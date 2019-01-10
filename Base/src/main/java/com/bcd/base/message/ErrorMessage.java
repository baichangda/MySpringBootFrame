package com.bcd.base.message;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.i18n.I18NData;

/**
 * Created by Administrator on 2017/7/26.
 */
public class ErrorMessage extends Message{
    public ErrorMessage(String msg) {
        super(msg);
    }

    public ErrorMessage(I18NData i18NData) {
        super(i18NData);
    }

    public ErrorMessage(String msg, String code) {
        super(msg, code);
    }

    public ErrorMessage(I18NData i18NData, String code) {
        super(i18NData, code);
    }

    public JsonMessage toJsonMessage(Object ... params) {
        return JsonMessage.fail(getValue(params),code);
    }

    public BaseRuntimeException toRuntimeException(Object ... params) {
        return new BaseRuntimeException(getValue(params),code);
    }

    public static ErrorMessage getMessage(String msg){
        return new ErrorMessage(msg);
    }
    public static ErrorMessage getMessage(String msg, String code){
        return new ErrorMessage(msg,code);
    }
    public static ErrorMessage getMessage(I18NData i18NData){
        return new ErrorMessage(i18NData);
    }
    public static ErrorMessage getMessage(I18NData i18NData, String code){
        return new ErrorMessage(i18NData,code);
    }
}
