package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class BatchUpdateSqlResult {
    private final String sql;
    private final List<Object[]> paramList;

    public BatchUpdateSqlResult(String sql, List<Object[]> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    public List<Object[]> getParamList() {
        return paramList;
    }

    public String getSql() {
        return sql;
    }

}
