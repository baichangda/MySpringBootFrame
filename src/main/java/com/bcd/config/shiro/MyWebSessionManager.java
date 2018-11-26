package com.bcd.config.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;


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
            ServletResponse response = WebUtils.getResponse(context);
            Serializable sessionId = session.getId();
            WebUtils.toHttp(response).setHeader(sessionHeaderKeyName,sessionId.toString());
        }
    }

    @Override
    protected Serializable getSessionId(SessionKey key) {
        Serializable id = super.getSessionId(key);
        if (id == null && WebUtils.isWeb(key)) {
            ServletRequest request = WebUtils.getRequest(key);
            id = this.getSessionId(request);
        }
        return id;
    }


    private Serializable getSessionId(ServletRequest request){
       return WebUtils.toHttp(request).getHeader(sessionHeaderKeyName);
    }
}
