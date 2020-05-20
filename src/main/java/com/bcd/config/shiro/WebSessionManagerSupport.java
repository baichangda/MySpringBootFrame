package com.bcd.config.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

public interface WebSessionManagerSupport {
    Serializable getSessionId(ServletRequest request, ServletResponse response);
}
