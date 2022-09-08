package com.bcd.base.support_satoken.anno;

import cn.dev33.satoken.annotation.SaMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaCheckNotePermissions {
    /**
     * 多账号体系下所属的账号体系标识
     * @return see note
     */
    String type() default "";

    NotePermission[] value();

    SaMode mode() default SaMode.AND;
}
