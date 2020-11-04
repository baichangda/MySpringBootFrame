package com.bcd.rdb.dbinfo.pgsql.bean;

import lombok.Data;

@Data
public class TablesBean {
    private String table_name;
    private String table_comment;
}
