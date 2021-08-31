package com.bcd.base.support_jdbc.sql;

import java.util.Map;

public class SqlMapResult {
    private String sql;
    private Map<String, Object> paramMap;

    public SqlMapResult(String sql, Map<String, Object> paramMap) {
        this.sql = sql;
        this.paramMap = paramMap;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
