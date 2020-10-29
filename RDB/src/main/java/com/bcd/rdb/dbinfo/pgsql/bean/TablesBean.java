package com.bcd.rdb.dbinfo.pgsql.bean;


public class TablesBean {
    private String table_name;
    private String table_comment;

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getTable_comment() {
        return table_comment;
    }

    public void setTable_comment(String table_comment) {
        this.table_comment = table_comment;
    }
}
