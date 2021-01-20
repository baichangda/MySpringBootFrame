package com.bcd.rdb.dbinfo.mysql.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Getter
@Setter
public class TablesBean {
    private String table_catalog;
    private String table_schema;
    private String table_name;
    private String table_type;
    private String engine;
    private Long version;
    private String row_format;
    private Long table_rows;
    private Long avg_row_length;
    private Long data_length;
    private Long max_data_length;
    private Long index_length;
    private Long data_free;
    private Long auto_increment;
    private Date create_time;
    private Date update_time;
    private Date check_time;
    private String table_collation;
    private Long checksum;
    private String create_options;
    private String table_comment;

}
