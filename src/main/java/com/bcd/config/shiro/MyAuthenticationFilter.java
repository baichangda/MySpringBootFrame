package com.bcd.config.shiro;

import com.bcd.base.util.JsonUtil;
import com.bcd.define.ErrorDefine;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 自定义的authc的过滤器，替换shiro默认的过滤器
 */
public class MyAuthenticationFilter extends AuthenticationFilter{
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if(!response.isCommitted()){
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(
                    JsonUtil.toDefaultJSONString(JsonUtil.toDefaultJSONString(ErrorDefine.ERROR_SHIRO_UNAUTHENTICATED.toJsonMessage()))
            );
        }
        return false;
    }
}
