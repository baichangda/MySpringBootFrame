package com.bcd.sys.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.BaseSuccessMessage;

public class SuccessDefine {
    public final static BaseSuccessMessage SUCCESS_RESET_PASSWORD=BaseSuccessMessage.getMessage(I18NData.getI18NData("UserController.resetPassword.SUCCESSED"));
    public final static BaseSuccessMessage SUCCESS_AUTHORITY=BaseSuccessMessage.getMessage(I18NData.getI18NData("UserController.runAs.SUCCESSED"));
    public final static BaseSuccessMessage SUCCESS_RELEASE=BaseSuccessMessage.getMessage(I18NData.getI18NData("UserController.releaseRunAs.SUCCESSED"));
    public final static BaseSuccessMessage SUCCESS_LOGOUT=BaseSuccessMessage.getMessage(I18NData.getI18NData("UserController.logout.SUCCESSED"));
}
