package com.bcd.base.support_shiro;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import com.bcd.sys.define.CommonConst;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * 自定义的authc的过滤器，替换shiro默认的过滤器
 * 继承此类重写 验证不通过的方法,是为了处理失败时候返回的结果集
 * 流程如下:
 * spring容器内发生异常逻辑如下:
 *
 * @RequiresAuthentication 注解发生异常->spring的CustomExceptionHandler拦截转换为结果集->.....
 * 在spring中发生异常后,异常会在spring容器内就转换为结果集,而不会抛出到过滤器链来,所以是不会触发onAccessDenied方法
 * <p>
 * 非spring容器发生异常逻辑如下
 * 过滤器链发生异常->调用过滤器的onAccessDenied方法处理
 * <p>
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
public class MyAuthenticationFilter extends AuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(MyAuthenticationFilter.class);
    private final ExceptionResponseHandler handler;
    private final WebSessionManagerSupport webSessionManagerSupport;
    private final RedisTemplate<String, String> redisTemplate;

    public MyAuthenticationFilter(ExceptionResponseHandler handler, WebSessionManagerSupport webSessionManagerSupport,
                                  RedisTemplate<String, String> redisTemplate) {
        this.handler = handler;
        this.webSessionManagerSupport = webSessionManagerSupport;
        this.redisTemplate = redisTemplate;
    }


    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (request.getAttribute("timeout") == null) {
            //获取sessionId
            Serializable sessionId = webSessionManagerSupport.getSessionId(request, response);
            if (sessionId == null) {
                handler.handle(WebUtils.toHttp(response), ShiroMessageDefine.ERROR_SHIRO_UNAUTHENTICATED.toJsonMessage());
            } else {
                //此时先检测是否有被踢出标志
                String key = CommonConst.KICK_SESSION_ID_PRE + sessionId.toString();
                String kickMessage = redisTemplate.opsForValue().get(key);
                if (kickMessage == null) {
                    handler.handle(WebUtils.toHttp(response), ShiroMessageDefine.ERROR_SHIRO_UNAUTHENTICATED.toJsonMessage());
                } else {
                    //标志获取后删除
                    redisTemplate.delete(key);
                    handler.handle(WebUtils.toHttp(response), JsonMessage.fail().message(kickMessage));
                }
            }
        } else {
            //处理session过期异常返回信息
            handler.handle(WebUtils.toHttp(response), ShiroMessageDefine.ERROR_SHIRO_EXPIREDSESSIONEXCEPTION.toJsonMessage());
        }
        return false;
    }
}
