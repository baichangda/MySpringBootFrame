package com.bcd.rdb.jdbc;

import java.util.List;

public class SqlListResult {
    private String sql;
    private List<Object> paramList;

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
