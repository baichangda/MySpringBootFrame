package com.bcd.base.support_satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
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

import java.lang.reflect.Method;
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
    private void registerSaAnnotationHandler() {
        SaAnnotationStrategy.instance.registerAnnotationHandler(new SaAnnotationHandlerInterface<SaCheckAction>() {
            @Override
            public Class<SaCheckAction> getHandlerAnnotationClass() {
                return SaCheckAction.class;
            }

            @Override
            public void checkMethod(SaCheckAction anno, Method method) {
                final String className = method.getDeclaringClass().getName();
                final String methodName = method.getName();
                SaManager.getStpLogic(anno.type(), false).checkPermissionAnd(className + ":" + methodName);
            }
        });

        SaAnnotationStrategy.instance.registerAnnotationHandler(new SaAnnotationHandlerInterface<SaCheckRequestMappingUrl>() {
            @Override
            public Class<SaCheckRequestMappingUrl> getHandlerAnnotationClass() {
                return SaCheckRequestMappingUrl.class;
            }

            @Override
            public void checkMethod(SaCheckRequestMappingUrl anno, Method method) {
                Class<?> clazz = method.getDeclaringClass();
                RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                String[] classUrls = classRequestMapping.value();
                String[] methodUrls = methodRequestMapping.value();
                Set<String> permissionSet = new HashSet<>();
                Arrays.stream(classUrls).forEach(e1 -> Arrays.stream(methodUrls).forEach(e2 -> permissionSet.add(e1 + e2)));
                SaManager.getStpLogic(anno.type(), false).checkPermissionOr(permissionSet.toArray(new String[0]));
            }
        });

        SaAnnotationStrategy.instance.registerAnnotationHandler(new SaAnnotationHandlerInterface<SaCheckNotePermissions>() {
            @Override
            public Class<SaCheckNotePermissions> getHandlerAnnotationClass() {
                return SaCheckNotePermissions.class;
            }

            @Override
            public void checkMethod(SaCheckNotePermissions anno, Method method) {
                String[] perms = Arrays.stream(anno.value()).map(e -> e.code).toArray(String[]::new);
                final StpLogic stpLogic = SaManager.getStpLogic(anno.type(), false);
                final SaMode mode = anno.mode();
                if (mode == SaMode.AND) {
                    stpLogic.checkPermissionAnd(perms);
                } else {
                    stpLogic.checkPermissionOr(perms);
                }
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerSaAnnotationHandler();
    }
}
