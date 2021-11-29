package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class CreateSqlResult {
    private final String sql;
    private final List paramList;

    public CreateSqlResult(String sql, List paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    public String getSql() {
        return sql;
    }

    public List getParamList() {
        return paramList;
    }

}
