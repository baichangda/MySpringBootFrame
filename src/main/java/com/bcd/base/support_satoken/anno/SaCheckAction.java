package com.bcd.base.support_satoken.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解使用方法为
 * 检查当前用户权限字符串中是否有当前方法的 [完整类名:方法名] 字符串
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaCheckAction {
    /**
     * 多账号体系下所属的账号体系标识
     * @return see note
     */
    String type() default "";
}
