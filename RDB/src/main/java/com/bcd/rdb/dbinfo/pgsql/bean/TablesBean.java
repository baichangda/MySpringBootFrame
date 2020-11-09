package com.bcd.rdb.dbinfo.pgsql.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class TablesBean {
    private String table_name;
    private String table_comment;
}
