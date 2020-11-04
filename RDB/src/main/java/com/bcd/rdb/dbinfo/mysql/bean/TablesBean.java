package com.bcd.rdb.dbinfo.mysql.bean;

import lombok.Data;

import java.util.Date;

@Data
public class TablesBean {
    private String table_catalog;
    private String table_schema;
    private String table_name;
    private String table_type;
    private String engine;
    private String version;
    private String row_format;
    private String table_rows;
    private String avg_row_length;
    private String data_length;
    private String max_data_length;
    private String index_length;
    private String data_free;
    private String auto_increment;
    private Date create_time;
    private Date update_time;
    private Date check_time;
    private String table_collation;
    private Long checksum;
    private String create_options;
    private String table_comment;

}
