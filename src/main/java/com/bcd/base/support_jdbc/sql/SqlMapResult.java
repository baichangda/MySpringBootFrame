package com.bcd.base.support_jdbc.sql;

import java.util.Map;

public class SqlMapResult {
    public final String sql;
    public final Map<String, Object> paramMap;

    public SqlMapResult(String sql, Map<String, Object> paramMap) {
        this.sql = sql;
        this.paramMap = paramMap;
    }

}
