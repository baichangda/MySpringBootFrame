package com.base.message;

import com.base.i18n.I18NData;
import com.base.json.JsonMessage;

/**
 * Created by Administrator on 2017/7/26.
 */
public abstract class BaseErrorMessage {
    public abstract JsonMessage toJsonMessage();

    public static BaseErrorMessage getMessage(String msg){
        return new DefaultErrorMessage(msg);
    }
    public static BaseErrorMessage getMessage(String msg,String code){
        return new DefaultErrorMessage(msg,code);
    }
    public static BaseErrorMessage getMessage(I18NData i18NData){
        return new DefaultErrorMessage(i18NData);
    }
    public static BaseErrorMessage getMessage(I18NData i18NData,String code){
        return new DefaultErrorMessage(i18NData,code);
    }
}
