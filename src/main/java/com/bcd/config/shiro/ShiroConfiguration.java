package com.bcd.config.shiro;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.base.config.shiro.AuthorizationHandler;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.*;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 1.LifecycleBeanPostProcessor，这是个DestructionAwareBeanPostProcessor的子类，负责org.apache.shiro.util.Initializable类型bean的生命周期的，初始化和销毁。主要是AuthorizingRealm类的子类，以及EhCacheManager类。
 2.HashedCredentialsMatcher，这个类是为了对密码进行编码的，防止密码在数据库里明码保存，当然在登陆认证的生活，这个类也负责对form里输入的密码进行编码。
 3.ShiroRealm，这是个自定义的认证类，继承自AuthorizingRealm，负责用户的认证和权限的处理，可以参考JdbcRealm的实现。
 4.EhCacheManager，缓存管理，用户登陆成功后，把用户信息和权限信息缓存起来，然后每次用户请求时，放入用户的session中，如果不设置这个bean，每个请求都会查询一次数据库。
 5.SecurityManager，权限管理，这个类组合了登陆，登出，权限，session的处理，是个比较重要的类。
 6.ShiroFilterFactoryBean，是个factorybean，为了生成ShiroFilter。它主要保持了三项数据，securityManager，filters，filterChainDefinitionManager。
 7.DefaultAdvisorAutoProxyCreator，Spring的一个bean，由Advisor决定对哪些类的方法进行AOP代理。
 8.AuthorizationAttributeSourceAdvisor，shiro里实现的Advisor类，内部使用AopAllianceAnnotationsAuthorizingMethodInterceptor来拦截用以下注解的方法。

 ShiroFilterFactoryBean 处理拦截资源文件问题。
 注意：单独一个ShiroFilterFactoryBean配置是或报错的，因为在
 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager

 Filter Chain定义说明
 1、一个URL可以配置多个Filter，使用逗号分隔
 2、当设置多个过滤器时，全部验证通过，才视为通过
 3、部分过滤器可指定参数，如perms，roles
 */
@Configuration
public class ShiroConfiguration{
    private static final Logger logger = LoggerFactory.getLogger(ShiroConfiguration.class);
    /**
     * 缓存管理器
     * @return
     */
    @Bean
    public EhCacheManager ehCacheManager(){
        EhCacheManager ehcacheManager = new EhCacheManager();
        ehcacheManager.setCacheManagerConfigFile("classpath:com/bcd/config/ehcache-shiro.xml");
        return ehcacheManager;
    }


    /**
     * 安全管理器
     * @param realm
     * @return
     */
    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(AuthorizingRealm realm, SessionManager sessionManager, EhCacheManager ehCacheManager){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //设置realm
        securityManager.setRealm(realm);
        //设置rememberMeManager
        RememberMeManager rememberMeManager=new MyCookieRememberMeManager();
//        RememberMeManager rememberMeManager=new MyWebHeaderRememberMeManager();
        securityManager.setRememberMeManager(rememberMeManager);
        //设置sessionManager从redis中获取
        securityManager.setSessionManager(sessionManager);
        //设置缓存管理器
        securityManager.setCacheManager(ehCacheManager);
        return securityManager;
    }

    /**
     * 会话管理器
     * @param redisTemplate
     * @return
     */
    @Bean
    public SessionManager sessionManager(@Qualifier(value = "string_serializable_redisTemplate") RedisTemplate redisTemplate){
//        MyWebHeaderSessionManager sessionManager=new MyWebHeaderSessionManager();
        DefaultWebSessionManager sessionManager=new DefaultWebSessionManager();
        sessionManager.setSessionDAO(new MySessionRedisDAO(redisTemplate));
        sessionManager.setGlobalSessionTimeout(-1000L);
        return sessionManager;
    }

    /**
     * 自定义权限注解解析器
     * @param securityManager
     * @param authorizationHandler 当前用户是否验证权限的处理器
     * @return
     */
    @Bean
    public MyAuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager, AuthorizationHandler authorizationHandler){
        MyAuthorizationAttributeSourceAdvisor advisor = new MyAuthorizationAttributeSourceAdvisor(authorizationHandler);
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * 拦截器工厂
     * @param securityManager
     * @return
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, ExceptionResponseHandler handler,AuthorizationHandler authorizationHandler){
        ShiroFilterFactoryBean factoryBean = new MyShiroFilterFactoryBean();
        Map<String,Filter> filterMap=new HashMap<>();
        filterMap.put("authc",new MyAuthenticationFilter(handler));
        filterMap.put("perms",new MyAuthorizationFilter(handler,authorizationHandler));
        filterMap.put("user",new MyUserFilter(handler));
        factoryBean.setFilters(filterMap);

        factoryBean.setSecurityManager(securityManager);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
//        factoryBean.setLoginUrl("/index.html");
        // 登录成功后要跳转的连接
//        factoryBean.setSuccessUrl("/welcome");
//        factoryBean.setUnauthorizedUrl("/403");
        loadShiroFilterChain(factoryBean);
        return factoryBean;
    }

    /**
     * shiro拦截链
     * 配置拦截规则
     * @param factoryBean
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean factoryBean) {
        /**下面这些规则配置最好配置到配置文件中*/
        Map<String, String> filterChainMap = new LinkedHashMap<>();
        //authc：该过滤器下的页面必须验证后才能访问，它是Shiro内置的一个拦截器
        //anon：它对应的过滤器里面是空的,什么都没做,可以理解为不拦截
        //user: authc后或者rememberMe的都可以访问
        filterChainMap.put("/api/anonymous/**", "anon");
        filterChainMap.put("/api/sys/user/login", "anon");
//        filterChainMap.put("/api/**/list", "user");
//        filterChainMap.put("/api/**/page", "user");
        filterChainMap.put("/api/**","authc");
        factoryBean.setFilterChainDefinitionMap(filterChainMap);
    }

    /**
     * 配置shiroFilter,此配置导致ShiroFilterFactoryBean无效
     * 主要是用于处理 filter 支持异步请求
     * @param shiroFilterFactoryBean
     * @return
     * @throws Exception
     */
    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean(@Qualifier(value = "shiroFilter") ShiroFilterFactoryBean shiroFilterFactoryBean) throws Exception{
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter((Filter)(shiroFilterFactoryBean.getObject()));
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setAsyncSupported(true);
        filterRegistration.setEnabled(true);
        filterRegistration.setDispatcherTypes(DispatcherType.REQUEST,DispatcherType.ASYNC);
        return filterRegistration;
    }
}