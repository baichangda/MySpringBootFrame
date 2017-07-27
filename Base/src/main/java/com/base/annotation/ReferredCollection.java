package com.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2017/6/8.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReferredCollection {
    String deleteHasRelationMessageKey() default "BaseBO.deleteWithNoReferred.FAILED";
    String deleteHasRelationMessageValue() default "删除失败,已被引用!";
}
