package com.bcd.base.config.shiro;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

public class ShiroMessageDefine {
    public final static ErrorMessage ERROR_SHIRO_UNKNOWN_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnknownAccountException"));
    public final static ErrorMessage ERROR_SHIRO_INCORRECT_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.IncorrectCredentialsException"));
    public final static ErrorMessage ERROR_SHIRO_DISABLED_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.DisabledAccountException"));
    public final static ErrorMessage ERROR_SHIRO_AUTHENTICATION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthenticationException"));
    public final static ErrorMessage ERROR_SHIRO_UNAUTHENTICATED = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnauthenticatedException"),String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_UNAUTHORIZEDEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnauthorizedException"),String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_EXPIRED_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.ExpiredCredentialsException"));
    public final static ErrorMessage ERROR_SHIRO_AUTHORIZATION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthorizationException"));
    public final static ErrorMessage ERROR_SHIRO_UNKNOWNSESSIONEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnknownSessionException"));
    public final static ErrorMessage ERROR_SHIRO_EXPIREDSESSIONEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.ExpiredSessionException"));
}
