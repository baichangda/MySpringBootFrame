package com.bcd.rdb.dbinfo.pgsql.bean;

import lombok.Data;

@Data
public class ColumnsBean {
    private String table_catalog;
    private String table_schema;
    private String table_name;
    private String column_name;
    private Integer ordinal_position;
    private String column_default;
    private String is_nullable;
    private String data_type;
    private Integer character_maximum_length;
    private Integer character_octet_length;
    private Integer numeric_precision;
    private Integer numeric_precision_radix;
    private Integer numeric_scale;
    private Integer datetime_precision;
    private String interval_type;
    private Integer interval_precision;
    private String character_set_catalog;
    private String character_set_schema;
    private String character_set_name;
    private String collation_catalog;
    private String collation_schema;
    private String collation_name;
    private String domain_catalog;
    private String domain_schema;
    private String domain_name;
    private String udt_catalog;
    private String udt_schema;
    private String udt_name;
    private String scope_catalog;
    private String scope_schema;
    private String scope_name;
    private Integer maximum_cardinality;
    private String dtd_identifier;
    private String is_self_referencing;
    private String is_identity;
    private String identity_generation;
    private String identity_start;
    private Long identity_increment;
    private Integer identity_maximum;
    private Integer identity_minimum;
    private String identity_cycle;
    private String is_generated;
    private String generation_expression;
    private String is_updatable;
    private Long objoid;
    private Long classoid;
    private Integer objsubid;
    private String description;


}
