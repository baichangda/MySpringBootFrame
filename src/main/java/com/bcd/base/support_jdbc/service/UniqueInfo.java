package com.bcd.base.support_jdbc.service;

import com.bcd.base.support_jdbc.anno.Unique;
import com.bcd.base.util.StringUtil;

public class UniqueInfo {
    public final FieldInfo fieldInfo;
    public final String msg;
    public final int code;
    public final String eqSql;

    public UniqueInfo(FieldInfo fieldInfo, Unique unique, String table) {
        this.fieldInfo = fieldInfo;
        this.msg = StringUtil.format(unique.msg(), fieldInfo.fieldName);
        this.code = unique.code();
        this.eqSql = StringUtil.format("select id from {} where {}=?", table, fieldInfo.columnName);
    }
}
