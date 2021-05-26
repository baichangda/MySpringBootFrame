package com.bcd.base.config.shiro;

import com.bcd.base.message.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

public class ShiroMessageDefine {
    public final static ErrorMessage ERROR_SHIRO_UNKNOWN_ACCOUNT = ErrorMessage.getMessage("不存在的用户名");
    public final static ErrorMessage ERROR_SHIRO_INCORRECT_CREDENTIALS = ErrorMessage.getMessage("密码错误");
    public final static ErrorMessage ERROR_SHIRO_DISABLED_ACCOUNT = ErrorMessage.getMessage("帐号被禁用");
    public final static ErrorMessage ERROR_SHIRO_AUTHENTICATION = ErrorMessage.getMessage("登录失败");
    public final static ErrorMessage ERROR_SHIRO_UNAUTHENTICATED = ErrorMessage.getMessage("请先登录").code(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_UNAUTHORIZEDEXCEPTION = ErrorMessage.getMessage("权限不足").code(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    public final static ErrorMessage ERROR_SHIRO_EXPIRED_CREDENTIALS = ErrorMessage.getMessage("凭证超时");
    public final static ErrorMessage ERROR_SHIRO_AUTHORIZATION = ErrorMessage.getMessage("权限不足");
    public final static ErrorMessage ERROR_SHIRO_UNKNOWNSESSIONEXCEPTION = ErrorMessage.getMessage("未知的会话,请重新登录");
    public final static ErrorMessage ERROR_SHIRO_EXPIREDSESSIONEXCEPTION = ErrorMessage.getMessage("会话已过期,请重新登陆");
}
