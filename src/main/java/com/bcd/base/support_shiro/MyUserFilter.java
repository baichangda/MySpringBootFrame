package com.bcd.base.support_shiro;

import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MyUserFilter extends UserFilter {
    private ExceptionResponseHandler handler;

    public MyUserFilter(ExceptionResponseHandler handler) {
        this.handler = handler;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        handler.handle(WebUtils.toHttp(response), ShiroMessageDefine.ERROR_SHIRO_UNAUTHENTICATED.toJsonMessage());
        return false;
    }
}
