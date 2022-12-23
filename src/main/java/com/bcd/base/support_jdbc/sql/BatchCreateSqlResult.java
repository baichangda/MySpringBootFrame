package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class BatchCreateSqlResult {
    public final String sql;
    public final List<Object[]> paramList;

    public BatchCreateSqlResult(String sql, List<Object[]> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

}
