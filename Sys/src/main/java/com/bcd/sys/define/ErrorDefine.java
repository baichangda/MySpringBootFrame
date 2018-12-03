package com.bcd.sys.define;


import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.BaseErrorMessage;

/**
 * Created by Administrator on 2017/9/28.
 */
public class ErrorDefine {
    public final static BaseErrorMessage ERROR_PASSWORD_WRONG=BaseErrorMessage.getMessage(I18NData.getI18NData("UserController.updatePassword.passwordWrong"));
    public final static BaseErrorMessage ERROR_HAS_RUN_AS=BaseErrorMessage.getMessage(I18NData.getI18NData("UserService.runAs.hasRunAs"));
    public final static BaseErrorMessage ERROR_NOT_RUN_AS=BaseErrorMessage.getMessage(I18NData.getI18NData("UserService.runAs.notRunAs"));
}
