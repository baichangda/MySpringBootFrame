package com.bcd.base.config.shiro;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

public class ShiroMessageDefine {
    public final static ErrorMessage ERROR_SHIRO_UNKNOWN_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.UnknownAccountException"));
    public final static ErrorMessage ERROR_SHIRO_INCORRECT_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.IncorrectCredentialsException"));
    public final static ErrorMessage ERROR_SHIRO_DISABLED_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.DisabledAccountException"));
    public final static ErrorMessage ERROR_SHIRO_AUTHENTICATION = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.AuthenticationException"));
    public final static ErrorMessage ERROR_SHIRO_UNAUTHENTICATED = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.UnauthenticatedException")).withCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_UNAUTHORIZEDEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.UnauthorizedException")).withCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_EXPIRED_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.ExpiredCredentialsException"));
    public final static ErrorMessage ERROR_SHIRO_AUTHORIZATION = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.AuthorizationException"));
    public final static ErrorMessage ERROR_SHIRO_UNKNOWNSESSIONEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.UnknownSessionException"));
    public final static ErrorMessage ERROR_SHIRO_EXPIREDSESSIONEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("ShiroException.ExpiredSessionException"));
}
