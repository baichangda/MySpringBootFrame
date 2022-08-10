package com.bcd.base.support_jdbc.rowmapper;

import cn.hutool.core.util.StrUtil;
import org.springframework.jdbc.core.ColumnMapRowMapper;

public class MyColumnMapRowMapper extends ColumnMapRowMapper {
    public final static MyColumnMapRowMapper ROW_MAPPER = new MyColumnMapRowMapper();

    @Override
    protected String getColumnKey(String columnName) {
        return StrUtil.toCamelCase(columnName,'_');
    }
}
