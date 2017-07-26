package com.config.shiro;


import com.base.i18n.I18NData;
import com.base.message.ErrorMessage;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/7/26.
 */
public class ShiroConst {
    public static HashMap<String, ErrorMessage> EXCEPTION_ERRORMESSAGE_MAP = new HashMap<>();

    static {
        //配置shiro的异常对应的ErrorMessage
        EXCEPTION_ERRORMESSAGE_MAP.put(UnknownAccountException.class.getName(), new ErrorMessage(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED),new I18NData("CustomExceptionHandler.UnknownAccountException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(IncorrectCredentialsException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.IncorrectCredentialsException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(DisabledAccountException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.DisabledAccountException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(AuthenticationException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.AuthenticationException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(UnauthenticatedException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.UnauthenticatedException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(ExpiredCredentialsException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.ExpiredCredentialsException")));
        EXCEPTION_ERRORMESSAGE_MAP.put(AuthorizationException.class.getName(), new ErrorMessage(new I18NData("CustomExceptionHandler.AuthorizationException")));

    }
}
