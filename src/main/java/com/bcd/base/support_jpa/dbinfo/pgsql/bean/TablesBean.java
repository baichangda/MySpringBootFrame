package com.bcd.base.support_jpa.dbinfo.pgsql.bean;

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
