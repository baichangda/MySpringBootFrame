package com.bcd.base.support_jdbc.service;

import com.bcd.base.util.StringUtil;

import java.lang.reflect.Field;

public class FieldInfo {
    public final String fieldName;
    public final String columnName;

    public final Field field;

    public FieldInfo(Field field) {
        this.field = field;
        this.fieldName = field.getName();
        this.columnName = StringUtil.camelCaseToSplitChar(this.fieldName, '_');
    }
}