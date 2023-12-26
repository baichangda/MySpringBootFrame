package com.bcd.base.support_jdbc.rowmapper;

import com.bcd.base.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.ColumnMapRowMapper;

public class MyColumnMapRowMapper extends ColumnMapRowMapper {
    public final static MyColumnMapRowMapper ROW_MAPPER = new MyColumnMapRowMapper();

    @Override
    protected String getColumnKey(@NotNull String columnName) {
        return StringUtil.splitCharToCamelCase(columnName, '_');
    }
}
