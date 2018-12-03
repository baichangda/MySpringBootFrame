package com.bcd.config.shiro;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.subject.WebSubjectContext;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyWebHeaderRememberMeManager extends AbstractRememberMeManager {
    private static transient final Logger log = LoggerFactory.getLogger(MyWebHeaderRememberMeManager.class);

    public static final String DEFAULT_REMEMBER_ME_HEADER_KEY_NAME ="rememberMe";
    public static final String DEFAULT_AES_BASE64_KEY ="s2SE9y32PvLeYo+VGFpcKA==";

    private String rememberMeHeaderKeyName;

    public String getRememberMeHeaderKeyName() {
        return rememberMeHeaderKeyName;
    }

    public void setRememberMeHeaderKeyName(String rememberMeHeaderKeyName) {
        this.rememberMeHeaderKeyName = rememberMeHeaderKeyName;
    }

    public MyWebHeaderRememberMeManager(String rememberMeHeaderKeyName) {
        this.rememberMeHeaderKeyName = rememberMeHeaderKeyName;
        //为了避免集群时候多个服务器采用不一样的key,在此生成固定的key
        setCipherKey(Base64.decode(DEFAULT_AES_BASE64_KEY));
    }

    public MyWebHeaderRememberMeManager() {
        this(DEFAULT_REMEMBER_ME_HEADER_KEY_NAME);
    }

    @Override
    protected void forgetIdentity(Subject subject) {
        //需要遗忘时候删除掉response中的header
        if (WebUtils.isHttp(subject)) {
            HttpServletRequest request = WebUtils.getHttpRequest(subject);
            HttpServletResponse response = WebUtils.getHttpResponse(subject);
            response.setHeader(rememberMeHeaderKeyName,null);
        }
    }

    @Override
    protected void rememberSerializedIdentity(Subject subject, byte[] serialized) {
        if (!WebUtils.isHttp(subject)) {
            if (log.isDebugEnabled()) {
                String msg = "Subject argument is not an HTTP-aware instance.  This is required to obtain a servlet " +
                        "request and response in order to set the rememberMe header. Returning immediately and " +
                        "ignoring rememberMe operation.";
                log.debug(msg);
            }
            return;
        }


        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        HttpServletResponse response = WebUtils.getHttpResponse(subject);

        //base 64 encode it and store as a cookie:
        String base64 = Base64.encodeToString(serialized);

        //在最后,将header写入response中
        response.setHeader(rememberMeHeaderKeyName,base64);
    }

    @Override
    protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext) {
        if (!WebUtils.isHttp(subjectContext)) {
            if (log.isDebugEnabled()) {
                String msg = "SubjectContext argument is not an HTTP-aware instance.  This is required to obtain a " +
                        "servlet request and response in order to retrieve the rememberMe header. Returning " +
                        "immediately and ignoring rememberMe operation.";
                log.debug(msg);
            }
            return null;
        }

        WebSubjectContext wsc = (WebSubjectContext) subjectContext;
        if (isIdentityRemoved(wsc)) {
            return null;
        }

        HttpServletRequest request = WebUtils.getHttpRequest(wsc);
        HttpServletResponse response = WebUtils.getHttpResponse(wsc);

        String base64 = request.getHeader(rememberMeHeaderKeyName);

        if (base64 != null) {
            base64 = ensurePadding(base64);
            if (log.isTraceEnabled()) {
                log.trace("Acquired Base64 encoded identity [" + base64 + "]");
            }
            byte[] decoded = Base64.decode(base64);
            if (log.isTraceEnabled()) {
                log.trace("Base64 decoded byte array length: " + (decoded != null ? decoded.length : 0) + " bytes.");
            }

            //在最后,将header写入response中
            response.setHeader(rememberMeHeaderKeyName,base64);

            return decoded;
        } else {
            //no header set - new site visitor?
            return null;
        }


    }

    @Override
    public void forgetIdentity(SubjectContext subjectContext) {
        //需要遗忘时候删除掉response中的header
        if (WebUtils.isHttp(subjectContext)) {
            HttpServletRequest request = WebUtils.getHttpRequest(subjectContext);
            HttpServletResponse response = WebUtils.getHttpResponse(subjectContext);
            response.setHeader(rememberMeHeaderKeyName,null);
        }
    }


    private boolean isIdentityRemoved(WebSubjectContext subjectContext) {
        ServletRequest request = subjectContext.resolveServletRequest();
        if (request != null) {
            Boolean removed = (Boolean) request.getAttribute(ShiroHttpServletRequest.IDENTITY_REMOVED_KEY);
            return removed != null && removed;
        }
        return false;
    }

    /**
     * Sometimes a user agent will send the rememberMe cookie value without padding,
     * most likely because {@code =} is a separator in the cookie header.
     * <p/>
     * Contributed by Luis Arias.  Thanks Luis!
     *
     * @param base64 the base64 encoded String that may need to be padded
     * @return the base64 String padded if necessary.
     */
    private String ensurePadding(String base64) {
        int length = base64.length();
        if (length % 4 != 0) {
            StringBuilder sb = new StringBuilder(base64);
            for (int i = 0; i < length % 4; ++i) {
                sb.append('=');
            }
            base64 = sb.toString();
        }
        return base64;
    }
}
