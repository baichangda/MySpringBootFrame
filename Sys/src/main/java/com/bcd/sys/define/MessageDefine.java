package com.bcd.sys.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;
import com.bcd.base.message.Message;

public class MessageDefine{
    public final static Message SUCCESS_RESET_PASSWORD= Message.getMessage(I18NData.getI18NData("UserController.resetPassword.SUCCEEDED"));
    public final static Message SUCCESS_AUTHORITY= Message.getMessage(I18NData.getI18NData("UserController.runAs.SUCCEEDED"));
    public final static Message SUCCESS_RELEASE= Message.getMessage(I18NData.getI18NData("UserController.releaseRunAs.SUCCEEDED"));
    public final static Message SUCCESS_LOGOUT= Message.getMessage(I18NData.getI18NData("UserController.logout.SUCCEEDED"));
    public final static ErrorMessage ERROR_PASSWORD_WRONG= ErrorMessage.getMessage(I18NData.getI18NData("UserController.updatePassword.passwordWrong"));
    public final static ErrorMessage ERROR_HAS_RUN_AS= ErrorMessage.getMessage(I18NData.getI18NData("UserService.runAs.hasRunAs"));
    public final static ErrorMessage ERROR_NOT_RUN_AS= ErrorMessage.getMessage(I18NData.getI18NData("UserService.runAs.notRunAs"));
}
