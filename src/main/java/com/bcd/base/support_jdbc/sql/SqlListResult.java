package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class SqlListResult {
    private final String sql;
    private final List<Object> paramList;

    public SqlListResult(String sql, List<Object> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParamList() {
        return paramList;
    }
}
