package com.bcd.base.support_jdbc.sql;

import java.util.List;

public class CreateSqlResult {
    public final String sql;
    public final List paramList;

    public CreateSqlResult(String sql, List paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

}
