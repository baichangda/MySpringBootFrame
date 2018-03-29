package com.bcd.config.shiro;

import com.bcd.base.json.JsonMessage;
import com.bcd.base.message.BaseErrorMessage;
import com.bcd.base.util.ExceptionUtil;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义的authc的过滤器，替换shiro默认的过滤器
 * 继承此类重写 验证不通过的方法,是为了处理失败时候返回的结果集
 * 流程如下:
 * spring容器内发生异常逻辑如下:
 * @RequiresAuthentication 注解发生异常->spring的CustomExceptionHandler拦截转换为结果集->.....
 * 在spring中发生异常后,异常会在spring容器内就转换为结果集,而不会抛出到过滤器链来,所以是不会触发onAccessDenied方法
 *
 * 非spring容器发生异常逻辑如下
 * 过滤器链发生异常->调用过滤器的onAccessDenied方法处理
 *
 * 如下此应用场景:
 * Map<String, String> filterChainMap = new LinkedHashMap<String, String>();
 * filterChainMap.put("/api/**","authc, roles[admin,user], perms[file:edit]");
 * factoryBean.setFilterChainDefinitionMap(filterChainMap);
 * 此时相当于:
 * 访问/api/**
 * 必须走authc过滤器(验证用户登陆)
 * 必须走roles过滤器(验证有角色 admin,user)
 * 必须走perms过滤器(验证有权限 file:edit)
 */
@SuppressWarnings("unchecked")
public class MyAuthenticationFilter extends BasicHttpAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(MyAuthenticationFilter.class);
    private HttpMessageConverter converter;

    public MyAuthenticationFilter(HttpMessageConverter converter) {
        this.converter=converter;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
//        boolean loggedIn = false; //false by default or we wouldn't be in this method
//        if (isLoginAttempt(request, response)) {
//            loggedIn = executeLogin(request, response);
//        }
//        if (!loggedIn) {
            response(response,new UnauthenticatedException());
//        }
//        return loggedIn;
        return false;
    }

    /**
     * 重写这个方法的原因是为了拦截所有的认证异常
     * 重写onAccessDenied不满足需求的原因是因为,父类cleanup只会处理UnauthenticatedException
     * @param request
     * @param response
     * @param existing
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {
        /**
         * 此处是 BasicHttpAuthenticationFilter cleanup逻辑,进行重写 start
         */
        try {
            /**
             * 此处是 onAccessDenied方法实现 start
             * */
            if(existing!=null){
//                boolean loggedIn = false; //false by default or we wouldn't be in this method
//                if (isLoginAttempt(request, response)) {
//                    loggedIn = executeLogin(request, response);
//                }
//                if (!loggedIn) {
                    response(response,existing);
//                }
                /**
                 * 此处是 onAccessDenied方法实现 end
                 * */
                existing = null;
            }
        } catch (Exception e) {
            existing = e;
        }
        /**
         * 此处是 BasicHttpAuthenticationFilter cleanup逻辑,进行重写 end
         */

        /**
         * 以下是 AdviceFilter cleanup逻辑,直接复制过来 start
         */
        Exception exception = existing;
        try {
            afterCompletion(request, response, exception);
            if (log.isTraceEnabled()) {
                log.trace("Successfully invoked afterCompletion method.");
            }
        } catch (Exception e) {
            if (exception == null) {
                exception = e;
            } else {
                log.debug("afterCompletion implementation threw an exception.  This will be ignored to " +
                        "allow the original source exception to be propagated.", e);
            }
        }
        if (exception != null) {
            if (exception instanceof ServletException) {
                throw (ServletException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                if (log.isDebugEnabled()) {
                    String msg = "Filter execution resulted in an unexpected Exception " +
                            "(not IOException or ServletException as the Filter API recommends).  " +
                            "Wrapping in ServletException and propagating.";
                    log.debug(msg);
                }
                throw new ServletException(exception);
            }
        }
        /**
         * 以下是 AdviceFilter cleanup逻辑,直接复制过来 end
         */
    }

    /**
     * 采用 spring 自带的转换器转换结果,输出结果
     * @param response
     * @param exception
     * @throws IOException
     */
    private void response(ServletResponse response,Exception exception) throws IOException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        ServletServerHttpResponse servletServerHttpResponse=new ServletServerHttpResponse(httpResponse);
        BaseErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(exception.getClass().getName());
        JsonMessage result;
        if(errorMessage==null){
            result= ExceptionUtil.toJsonMessage(exception);
        }else{
            result=errorMessage.toJsonMessage();
        }
        converter.write(result,
                MediaType.APPLICATION_JSON_UTF8,
                servletServerHttpResponse);
    }
}
