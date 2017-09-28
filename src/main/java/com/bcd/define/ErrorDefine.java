package com.bcd.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.BaseErrorMessage;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2017/9/28.
 */
public class ErrorDefine {
   public static BaseErrorMessage ERROR_SHIRO_UNKNOWN_ACCOUNT = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnknownAccountException"));
   public static BaseErrorMessage ERROR_SHIRO_INCORRECT_CREDENTIALS = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.IncorrectCredentialsException"));
   public static BaseErrorMessage ERROR_SHIRO_DISABLED_ACCOUNT = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.DisabledAccountException"));
   public static BaseErrorMessage ERROR_SHIRO_AUTHENTICATION = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthenticationException"));
   public static BaseErrorMessage ERROR_SHIRO_UNAUTHENTICATED = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.UnauthenticatedException"),String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
   public static BaseErrorMessage ERROR_SHIRO_EXPIRED_CREDENTIALS = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.ExpiredCredentialsException"));
   public static BaseErrorMessage ERROR_SHIRO_AUTHORIZATION = BaseErrorMessage.getMessage(I18NData.getI18NData("CustomExceptionHandler.AuthorizationException"));

   public static BaseErrorMessage ERROR_DATECONVERT_FAILED=BaseErrorMessage.getMessage(I18NData.getI18NData("DateConvert.convert.FAILED"));

   public static BaseErrorMessage ERROR_RABBITMQ_MESSAGECONVERT_UNSUPPORTEDENCODING=BaseErrorMessage.getMessage(I18NData.getI18NData("MyMessageConverter.fromMessage.UnsupportedEncoding"));

}
