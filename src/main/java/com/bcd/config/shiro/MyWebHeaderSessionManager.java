package com.bcd.config.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * 自定义Session Manager
 * 1、取出request header中的 对应sessionId 来识别身份
 * 2、生成新session时候,在response header中加入 sessionId
 *
 */
public class MyWebSessionManager extends DefaultSessionManager {
    private static final Logger log = LoggerFactory.getLogger(MyWebSessionManager.class);

    private String sessionHeaderKeyName;

    public MyWebSessionManager(String sessionHeaderKeyName) {
        this.sessionHeaderKeyName = sessionHeaderKeyName;
    }

    @Override
    protected void onStart(Session session, SessionContext context) {
        super.onStart(session, context);
        if (!WebUtils.isHttp(context)) {
            log.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response pair. No session ID header will be set.");
        } else {
            ServletRequest request = WebUtils.getRequest(context);
            ServletResponse response = WebUtils.getResponse(context);
            //session开始时候清空request中的 sessionId来源,设置当前session为新建状态
            request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
            //设置sessionId到响应头中
            Serializable sessionId = session.getId();
            WebUtils.toHttp(response).setHeader(sessionHeaderKeyName,sessionId.toString());
        }
    }

    @Override
    protected Serializable getSessionId(SessionKey key) {
        ServletRequest request = WebUtils.getRequest(key);
        Serializable id = super.getSessionId(key);
        if (id == null && WebUtils.isWeb(key)) {
            id = this.getSessionId(request);
        }
        if(id!=null&&WebUtils.isWeb(key)){
            //当id不为空时候,设置sessionId来源、值、有效性到request中
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,"header");
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID,id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID,Boolean.TRUE);
            ServletResponse response = WebUtils.getResponse(key);
            WebUtils.toHttp(response).setHeader(sessionHeaderKeyName, id.toString());
        }
        //设置防止重写url(将sessionId加在url后面)
        if(request!=null){
            request.setAttribute(ShiroHttpServletRequest.SESSION_ID_URL_REWRITING_ENABLED, Boolean.FALSE);
        }
        return id;
    }


    private Serializable getSessionId(ServletRequest request){
       return WebUtils.toHttp(request).getHeader(sessionHeaderKeyName);
    }


}
