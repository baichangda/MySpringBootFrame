package com.base.exception;

import com.base.message.DefaultErrorMessage;
import com.base.i18n.I18NData;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import org.springframework.util.StringUtils;

/**
 * Created by Administrator on 2017/7/26.
 */
public class DefaultRuntimeException extends BaseRuntimeException {
    private String code;
    private String msg;
    //对应的是message的I18NData
    private I18NData i18NData;

    public DefaultRuntimeException() {
    }

    public DefaultRuntimeException(String message) {
        this.msg = message;
    }

    public DefaultRuntimeException(I18NData i18NData) {
        this.i18NData = i18NData;
    }

    public DefaultRuntimeException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public DefaultRuntimeException(String code, I18NData i18NData) {
        this.code = code;
        this.i18NData = i18NData;
    }

    public DefaultRuntimeException(DefaultErrorMessage errorMessage){
        this(errorMessage.getCode(),errorMessage.getMsg());
    }

    @Override
    public JsonMessage toJsonMessage() {
        //如果message为空且i18nData不为空,则使用i18nData
        if(StringUtils.isEmpty(msg)&& i18NData!=null){
            msg=I18nUtil.getMessage(i18NData.getKey());
        }
        return JsonMessage.failed(msg,code);
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
