package com.bcd.base.support_satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import com.bcd.base.support_satoken.anno.SaCheckAction;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.base.support_satoken.anno.SaCheckRequestMappingUrl;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@Configuration
public class SaTokenConfig implements WebMvcConfigurer, ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 开启了
         * 1、/** 的注解鉴权、即所有controller接口都会被注解鉴权
         * 2、路由匹配的登陆验证
         * 具体逻辑查看 {@link  SaInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)}
         */
        registry.addInterceptor(new SaInterceptor(handler -> {
            StpUtil.checkLogin();
        })).addPathPatterns("/api/**").excludePathPatterns("/api/sys/user/login", "/api/anno");
    }

    /**
     * 自定义注解
     */
    private void rewriteCheckMethodAnnotation() {
        //重写aop注解式鉴权、实现自定义注解
        SaStrategy.instance.checkMethodAnnotation = method -> {
            // 再校验 Method 上的注解
            SaStrategy.instance.checkElementAnnotation.accept(method);

            // 校验 @SaCheckAction 注解
            final SaCheckAction saCheckAction = method.getAnnotation(SaCheckAction.class);
            if (saCheckAction != null) {
                final String className = method.getDeclaringClass().getName();
                final String methodName = method.getName();
                SaManager.getStpLogic(saCheckAction.type(), false).checkPermissionAnd(className + ":" + methodName);
            }

            // 校验 @SaCheckRequestMappingUrl 注解
            final SaCheckRequestMappingUrl saCheckRequestMappingUrl = method.getAnnotation(SaCheckRequestMappingUrl.class);
            if (saCheckRequestMappingUrl != null) {
                Class<?> clazz = method.getDeclaringClass();
                RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);

                String[] classUrls = classRequestMapping.value();
                String[] methodUrls = methodRequestMapping.value();

                Set<String> permissionSet = new HashSet<>();
                Arrays.stream(classUrls).forEach(e1 -> Arrays.stream(methodUrls).forEach(e2 -> permissionSet.add(e1 + e2)));

                SaManager.getStpLogic(saCheckRequestMappingUrl.type(), false).checkPermissionOr(permissionSet.toArray(new String[0]));
            }

            // 校验 @SaCheckNotePermissions 注解
            final SaCheckNotePermissions sacheckNotePermissions = method.getAnnotation(SaCheckNotePermissions.class);
            if (sacheckNotePermissions != null) {
                String[] perms = Arrays.stream(sacheckNotePermissions.value()).map(e -> e.code).toArray(String[]::new);
                final StpLogic stpLogic = SaManager.getStpLogic(sacheckNotePermissions.type(), false);
                final SaMode mode = sacheckNotePermissions.mode();
                if (mode == SaMode.AND) {
                    stpLogic.checkPermissionAnd(perms);
                } else {
                    stpLogic.checkPermissionOr(perms);
                }
            }
        };
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        rewriteCheckMethodAnnotation();
    }
}
