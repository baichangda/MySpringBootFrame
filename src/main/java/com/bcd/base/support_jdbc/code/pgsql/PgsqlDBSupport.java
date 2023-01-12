package com.bcd.base.support_jdbc.code.pgsql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.code.CodeConst;
import com.bcd.base.support_jdbc.code.DBSupport;
import com.bcd.base.support_jdbc.code.TableConfig;
import com.bcd.base.support_jdbc.code.data.BeanField;
import com.bcd.base.support_jdbc.dbinfo.data.DBInfo;
import com.bcd.base.support_jdbc.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.base.support_jdbc.dbinfo.pgsql.util.DBInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PgsqlDBSupport implements DBSupport {

    Logger logger = LoggerFactory.getLogger(PgsqlDBSupport.class);

    @Override
    public DBInfo getSpringDBConfig() {
        return DBInfoUtil.getDBProps();
    }

    @Override
    public List<BeanField> getTableBeanFieldList(TableConfig config, Connection connection) {
        String tableName = config.tableName;
        List<ColumnsBean> res = DBInfoUtil.findColumns(connection, config.config.dbInfo.db, tableName);
        return res.stream().map(e -> {
            PgsqlDBColumn pgsqlDBColumn = new PgsqlDBColumn();
            pgsqlDBColumn.name = e.column_name;
            pgsqlDBColumn.type = e.udt_name;
            pgsqlDBColumn.comment = e.description;
            pgsqlDBColumn.isNull = e.is_nullable;
            pgsqlDBColumn.strLen = e.character_maximum_length;
            BeanField beanField = pgsqlDBColumn.toBeanField();
            if (beanField == null) {
                logger.warn("不支持[table:{}] [name:{}] [type:{}]类型数据库字段,忽略此字段!", config.tableName, pgsqlDBColumn.name, pgsqlDBColumn.type);
            }
            return beanField;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public CodeConst.PkType getTablePkType(TableConfig config, Connection connection) {
        ColumnsBean pk = DBInfoUtil.findPKColumn(connection, config.config.dbInfo.db, config.tableName);
        switch (pk.udt_name) {
            case "int2":
            case "int4": {
                return CodeConst.PkType.Integer;
            }
            case "int8": {
                return CodeConst.PkType.Long;
            }
            case "varchar": {
                return CodeConst.PkType.String;
            }
            default: {
                throw BaseRuntimeException.getException("pk[{0},{1},{2}] not support", pk.table_name, pk.column_name, pk.udt_name);
            }
        }
    }
}
