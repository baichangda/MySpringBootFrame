package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class SqlListResult {
    public final String sql;
    public final List<Object> paramList;

    public SqlListResult(String sql, List<Object> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

}
