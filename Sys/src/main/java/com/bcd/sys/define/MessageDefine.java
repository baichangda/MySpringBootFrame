package com.bcd.sys.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.Message;

public class SuccessDefine {
    public final static Message SUCCESS_RESET_PASSWORD= Message.getMessage(I18NData.getI18NData("UserController.resetPassword.SUCCESSED"));
    public final static Message SUCCESS_AUTHORITY= Message.getMessage(I18NData.getI18NData("UserController.runAs.SUCCESSED"));
    public final static Message SUCCESS_RELEASE= Message.getMessage(I18NData.getI18NData("UserController.releaseRunAs.SUCCESSED"));
    public final static Message SUCCESS_LOGOUT= Message.getMessage(I18NData.getI18NData("UserController.logout.SUCCESSED"));
}
