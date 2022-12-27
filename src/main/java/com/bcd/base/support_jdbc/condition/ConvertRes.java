package com.bcd.base.support_jdbc.condition;

import java.util.List;

public class ConvertRes {
    public String sql;
    public final List paramList;
    public ConvertRes(String sql, List paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }
}
