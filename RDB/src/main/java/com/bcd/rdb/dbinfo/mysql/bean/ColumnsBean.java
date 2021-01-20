package com.bcd.rdb.dbinfo.mysql.bean;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class ColumnsBean {
    private String table_catalog;
    private String table_schema;
    private String table_name;
    private String column_name;
    private Integer ordinal_position;
    private String column_default;
    private String is_nullable;
    private String data_type;
    private Long character_maximum_length;
    private Long character_octet_length;
    private Integer numeric_precision;
    private Integer numeric_scale;
    private Integer datetime_precision;
    private String character_set_name;
    private String collation_name;
    private String column_type;
    private String column_key;
    private String extra;
    private String privileges;
    private String column_comment;
    private String generation_expression;
    private Integer srs_id;


}
