package com.bcd.config.shiro;

import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

public class MyDefaultWebSessionManager extends DefaultWebSessionManager implements WebSessionManagerSupport {
    /**
     * 重写此方法主要是为了在发送session过期时候,向request中添加标记
     * 便于在{@link MyAuthenticationFilter#onAccessDenied(ServletRequest, ServletResponse)}
     * 中判断是否是session过期来返回不同的错误信息
     */
    @Override
    protected void onExpiration(Session s, ExpiredSessionException ese, SessionKey key) {
        super.onExpiration(s, ese, key);
        WebUtils.getRequest(key).setAttribute("timeout",true);
    }

    @Override
    public Serializable getSessionId(ServletRequest request,ServletResponse response) {
        return super.getSessionId(request,response);
    }
}
