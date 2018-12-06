package com.bcd.rdb.dbinfo.mysql.bean;


public class ColumnsBean {
    private String table_catalog;
    private String table_schema;
    private String table_name;
    private String column_name;
    private Long ordinal_position;
    private String column_default;
    private String is_nullable;
    private String data_type;
    private Long character_maximum_length;
    private Long character_octet_length;
    private Long numeric_precision;
    private Long numeric_scale;
    private Long datetime_precision;
    private String character_set_name;
    private String collation_name;
    private String column_type;
    private String column_key;
    private String extra;
    private String privileges;
    private String column_comment;
    private String generation_expression;

    public String getTable_catalog() {
        return table_catalog;
    }

    public void setTable_catalog(String table_catalog) {
        this.table_catalog = table_catalog;
    }

    public String getTable_schema() {
        return table_schema;
    }

    public void setTable_schema(String table_schema) {
        this.table_schema = table_schema;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public Long getOrdinal_position() {
        return ordinal_position;
    }

    public void setOrdinal_position(Long ordinal_position) {
        this.ordinal_position = ordinal_position;
    }

    public String getColumn_default() {
        return column_default;
    }

    public void setColumn_default(String column_default) {
        this.column_default = column_default;
    }

    public String getIs_nullable() {
        return is_nullable;
    }

    public void setIs_nullable(String is_nullable) {
        this.is_nullable = is_nullable;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public Long getCharacter_maximum_length() {
        return character_maximum_length;
    }

    public void setCharacter_maximum_length(Long character_maximum_length) {
        this.character_maximum_length = character_maximum_length;
    }

    public Long getCharacter_octet_length() {
        return character_octet_length;
    }

    public void setCharacter_octet_length(Long character_octet_length) {
        this.character_octet_length = character_octet_length;
    }

    public Long getNumeric_precision() {
        return numeric_precision;
    }

    public void setNumeric_precision(Long numeric_precision) {
        this.numeric_precision = numeric_precision;
    }

    public Long getNumeric_scale() {
        return numeric_scale;
    }

    public void setNumeric_scale(Long numeric_scale) {
        this.numeric_scale = numeric_scale;
    }

    public Long getDatetime_precision() {
        return datetime_precision;
    }

    public void setDatetime_precision(Long datetime_precision) {
        this.datetime_precision = datetime_precision;
    }

    public String getCharacter_set_name() {
        return character_set_name;
    }

    public void setCharacter_set_name(String character_set_name) {
        this.character_set_name = character_set_name;
    }

    public String getCollation_name() {
        return collation_name;
    }

    public void setCollation_name(String collation_name) {
        this.collation_name = collation_name;
    }

    public String getColumn_type() {
        return column_type;
    }

    public void setColumn_type(String column_type) {
        this.column_type = column_type;
    }

    public String getColumn_key() {
        return column_key;
    }

    public void setColumn_key(String column_key) {
        this.column_key = column_key;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getPrivileges() {
        return privileges;
    }

    public void setPrivileges(String privileges) {
        this.privileges = privileges;
    }

    public String getColumn_comment() {
        return column_comment;
    }

    public void setColumn_comment(String column_comment) {
        this.column_comment = column_comment;
    }

    public String getGeneration_expression() {
        return generation_expression;
    }

    public void setGeneration_expression(String generation_expression) {
        this.generation_expression = generation_expression;
    }
}
