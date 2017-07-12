package com.base.annotation;

import java.lang.annotation.*;


/**
 * Created by Administrator on 2017/6/8.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReferCollection {
    String saveHasRepeatMessageKey() default "BaseBO.saveWithNoRepeatRefer.FAILED";
}
