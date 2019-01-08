package com.bcd.config.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.springframework.beans.factory.BeanInitializationException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MyShiroFilterFactoryBean extends ShiroFilterFactoryBean {
    /**
     * 忽略的请求结尾的url
     */
    private Set<String> ignoreExt;
  
    public MyShiroFilterFactoryBean(){  
        super();  
        ignoreExt = new HashSet<>();
        ignoreExt.add(".jpg");  
        ignoreExt.add(".png");  
        ignoreExt.add(".gif");  
        ignoreExt.add(".bmp");  
        ignoreExt.add(".js");  
        ignoreExt.add(".css");  
        ignoreExt.add(".html");
    }
    /**  
     * 启动时加载
     * 重写此方法,构造自己的过滤器
     */  
    @Override  
    protected AbstractShiroFilter createInstance() throws Exception {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager == null) {
            String msg = "SecurityManager property must be set.";
            throw new BeanInitializationException(msg);
        }

        if (!(securityManager instanceof WebSecurityManager)) {
            String msg = "The security manager does not implement the WebSecurityManager interface.";
            throw new BeanInitializationException(msg);
        }

        FilterChainManager manager = createFilterChainManager();

        //Expose the constructed FilterChainManager by first wrapping it in a
        // FilterChainResolver implementation. The AbstractShiroFilter implementations
        // do not know about FilterChainManagers - only resolvers:
        PathMatchingFilterChainResolver chainResolver = new PathMatchingFilterChainResolver();
        chainResolver.setFilterChainManager(manager);

        //Now onCreate a concrete ShiroFilter instance and apply the acquired SecurityManager and built
        //FilterChainResolver.  It doesn't matter that the instance is an anonymous inner class
        //here - we're just using it because it is a concrete AbstractShiroFilter instance that accepts
        //injection of the SecurityManager and FilterChainResolver:
        return new MySpringShiroFilter((WebSecurityManager) securityManager, chainResolver);
    }  
  
    /**  
     * 启动时加载  
     */  
    private class MySpringShiroFilter extends AbstractShiroFilter {  
        public MySpringShiroFilter(  
                WebSecurityManager securityManager, PathMatchingFilterChainResolver chainResolver) {  
            super();  
            if (securityManager == null){  
                throw new IllegalArgumentException("WebSecurityManager property cannot be null.");  
            }  
            setSecurityManager(securityManager);  
            if (chainResolver != null){  
                setFilterChainResolver(chainResolver);  
            }  
        }  
        /**  
         * 页面上传输的url先进入此方法验证  
         */  
        @Override  
        protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse,
                                        FilterChain chain)
                throws ServletException, IOException {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            String str = request.getRequestURI().toLowerCase();  
            boolean flag = true;  
            int idx;
            if ((idx = str.lastIndexOf('.')) > 0){
                str = str.substring(idx);  
                if (ignoreExt.contains(str.toLowerCase())){  
                    flag = false;  
                }  
            }
            if (flag){  
                super.doFilterInternal(servletRequest, servletResponse, chain);  
            } else {  
                chain.doFilter(servletRequest, servletResponse);  
            }  
        }  
    }  
}  