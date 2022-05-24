package com.bcd.base.support_satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.interceptor.SaAnnotationInterceptor;
import cn.dev33.satoken.interceptor.SaRouteInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaStrategy;
import com.bcd.base.support_satoken.anno.NotePermission;
import com.bcd.base.support_satoken.anno.SaCheckAction;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.base.support_satoken.anno.SaCheckRequestMappingUrl;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer, ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token的路由拦截器
        // 登陆拦截器
        registry.addInterceptor(new SaRouteInterceptor()).addPathPatterns("/api/**")
                .excludePathPatterns("/api/sys/user/login", "/api/anno");
        // 注解权限拦截器
        registry.addInterceptor(new SaAnnotationInterceptor()).addPathPatterns("/api/**");
    }

    /**
     * 自定义注解
     */
    private void rewriteCheckMethodAnnotation() {
        //重写aop注解式鉴权、实现自定义注解
        SaStrategy.me.checkMethodAnnotation = method -> {
            // 再校验 Method 上的注解
            SaStrategy.me.checkElementAnnotation.accept(method);

            // 校验 @SaCheckAction 注解
            final SaCheckAction saCheckAction = method.getAnnotation(SaCheckAction.class);
            if (saCheckAction != null) {
                final String className = method.getDeclaringClass().getName();
                final String methodName = method.getName();
                SaManager.getStpLogic(saCheckAction.type()).checkPermission(className + ":" + methodName);
            }

            // 校验 @SaCheckRequestMappingUrl 注解
            final SaCheckRequestMappingUrl saCheckRequestMappingUrl = method.getAnnotation(SaCheckRequestMappingUrl.class);
            if (saCheckRequestMappingUrl != null) {
                Class clazz = method.getDeclaringClass();
                RequestMapping classRequestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);

                String[] classUrls = classRequestMapping.value();
                String[] methodUrls = methodRequestMapping.value();

                Set<String> permissionSet = new HashSet<>();
                Arrays.stream(classUrls).forEach(e1 -> {
                    Arrays.stream(methodUrls).forEach(e2 -> {
                        permissionSet.add(e1 + e2);
                    });
                });

                SaManager.getStpLogic(saCheckRequestMappingUrl.type()).checkPermissionOr(permissionSet.toArray(new String[0]));
            }

            // 校验 @SaCheckNotePermissions 注解
            final SaCheckNotePermissions sacheckNotePermissions = method.getAnnotation(SaCheckNotePermissions.class);
            if (sacheckNotePermissions != null) {
                String[] perms = Arrays.stream(sacheckNotePermissions.value()).map(NotePermission::getCode).toArray(String[]::new);
                final StpLogic stpLogic = SaManager.getStpLogic(sacheckNotePermissions.type());
                if (perms.length == 1) {
                    stpLogic.checkPermission(perms[0]);
                } else {
                    final SaMode mode = sacheckNotePermissions.mode();
                    if (mode == SaMode.AND) {
                        stpLogic.checkPermissionAnd(perms);
                    } else {
                        stpLogic.checkPermissionOr(perms);
                    }
                }
            }
        };
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        rewriteCheckMethodAnnotation();
    }
}
