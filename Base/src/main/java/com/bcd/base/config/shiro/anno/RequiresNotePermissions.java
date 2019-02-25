package com.bcd.base.config.shiro.anno;

import com.bcd.base.config.shiro.data.NotePermission;
import org.apache.shiro.authz.annotation.Logical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresNotePermissions {
    NotePermission[] value();

    Logical logical() default Logical.AND;
}
