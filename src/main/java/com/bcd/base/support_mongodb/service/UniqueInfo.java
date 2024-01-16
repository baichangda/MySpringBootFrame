package com.bcd.base.support_mongodb.service;

import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.util.StringUtil;

import java.lang.reflect.Field;

public class UniqueInfo {
    public final Field field;
    public final String fieldName;
    public final String msg;

    public UniqueInfo(Field field) {
        Unique unique = field.getAnnotation(Unique.class);
        this.field = field;
        this.fieldName = field.getName();
        this.msg = StringUtil.format(unique.msg(), this.fieldName);
    }
}
