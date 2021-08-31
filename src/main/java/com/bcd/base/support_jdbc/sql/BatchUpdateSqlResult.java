package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class BatchUpdateSqlResult {
    private String sql;
    private List<Object[]> paramList;

    public BatchUpdateSqlResult(String sql, List<Object[]> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    public List<Object[]> getParamList() {
        return paramList;
    }

    public void setParamList(List<Object[]> paramList) {
        this.paramList = paramList;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}
