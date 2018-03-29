package com.bcd.config.shiro;


import com.bcd.base.message.BaseErrorMessage;
import com.bcd.config.define.ErrorDefine;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.session.UnknownSessionException;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/7/26.
 */
public class ShiroConst {
    public final static HashMap<String, BaseErrorMessage> EXCEPTION_ERRORMESSAGE_MAP = new HashMap<>();

    static {
        //配置shiro的异常对应的ErrorMessage
        EXCEPTION_ERRORMESSAGE_MAP.put(UnknownAccountException.class.getName(), ErrorDefine.ERROR_SHIRO_UNKNOWN_ACCOUNT);
        EXCEPTION_ERRORMESSAGE_MAP.put(IncorrectCredentialsException.class.getName(), ErrorDefine.ERROR_SHIRO_INCORRECT_CREDENTIALS);
        EXCEPTION_ERRORMESSAGE_MAP.put(DisabledAccountException.class.getName(), ErrorDefine.ERROR_SHIRO_DISABLED_ACCOUNT);
        EXCEPTION_ERRORMESSAGE_MAP.put(AuthenticationException.class.getName(), ErrorDefine.ERROR_SHIRO_AUTHENTICATION);
        EXCEPTION_ERRORMESSAGE_MAP.put(UnauthenticatedException.class.getName(), ErrorDefine.ERROR_SHIRO_UNAUTHENTICATED);
        EXCEPTION_ERRORMESSAGE_MAP.put(ExpiredCredentialsException.class.getName(), ErrorDefine.ERROR_SHIRO_EXPIRED_CREDENTIALS);
        EXCEPTION_ERRORMESSAGE_MAP.put(AuthorizationException.class.getName(), ErrorDefine.ERROR_SHIRO_AUTHORIZATION);
        EXCEPTION_ERRORMESSAGE_MAP.put(UnauthorizedException.class.getName(), ErrorDefine.ERROR_SHIRO_AUTHORIZATION);
        EXCEPTION_ERRORMESSAGE_MAP.put(UnknownSessionException.class.getName(), ErrorDefine.ERROR_SHIRO_UNKNOWNSESSIONEXCEPTION);
    }
}
