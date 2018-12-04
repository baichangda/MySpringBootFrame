package com.bcd.config.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2017/9/28.
 */
public class ErrorDefine {
   public static ErrorMessage ERROR_SHIRO_UNKNOWN_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnknownAccountException"));
   public static ErrorMessage ERROR_SHIRO_INCORRECT_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.IncorrectCredentialsException"));
   public static ErrorMessage ERROR_SHIRO_DISABLED_ACCOUNT = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.DisabledAccountException"));
   public static ErrorMessage ERROR_SHIRO_AUTHENTICATION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthenticationException"));
   public static ErrorMessage ERROR_SHIRO_UNAUTHENTICATED = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnauthenticatedException"),String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
   public static ErrorMessage ERROR_SHIRO_EXPIRED_CREDENTIALS = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.ExpiredCredentialsException"));
   public static ErrorMessage ERROR_SHIRO_AUTHORIZATION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthorizationException"));
   public static ErrorMessage ERROR_SHIRO_UNKNOWNSESSIONEXCEPTION = ErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnknownSessionException"));

}
