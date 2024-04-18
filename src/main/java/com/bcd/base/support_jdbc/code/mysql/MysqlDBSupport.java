package com.bcd.base.support_jdbc.code.mysql;

import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.code.DBSupport;
import com.bcd.base.support_jdbc.code.TableConfig;
import com.bcd.base.support_jdbc.code.data.BeanField;
import com.bcd.base.support_jdbc.dbinfo.data.DBInfo;
import com.bcd.base.support_jdbc.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.base.support_jdbc.dbinfo.mysql.util.DBInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MysqlDBSupport implements DBSupport {

    Logger logger = LoggerFactory.getLogger(MysqlDBSupport.class);

    @Override
    public DBInfo getSpringDBConfig() {
        return DBInfoUtil.getDBProps();
    }

    @Override
    public List<BeanField> getTableBeanFieldList(TableConfig config, Connection connection) {
        String tableName = config.tableName;
        List<ColumnsBean> res = DBInfoUtil.findColumns(connection, config.config.dbInfo.db, tableName);
        return res.stream().map(e -> {
            MysqlDBColumn mysqlDbColumn = new MysqlDBColumn();
            mysqlDbColumn.name = e.column_name;
            mysqlDbColumn.type = e.data_type;
            mysqlDbColumn.comment = e.column_comment;
            mysqlDbColumn.isNull = e.is_nullable;
            mysqlDbColumn.strLen = e.character_maximum_length.intValue();
            BeanField beanField = mysqlDbColumn.toBeanField();
            if (beanField == null) {
                logger.warn("不支持[table:{}] [name:{}] [type:{}]类型数据库字段,忽略此字段!", config.tableName, mysqlDbColumn.name, mysqlDbColumn.type);
            }
            return beanField;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    @Override
    public BeanField getTablePk(TableConfig config, Connection connection) {
        ColumnsBean pk = DBInfoUtil.findPKColumn(connection, config.config.dbInfo.db, config.tableName);
        switch (pk.data_type) {
            case "int", "bigint", "varchar" -> {
                MysqlDBColumn mysqlDbColumn = new MysqlDBColumn();
                mysqlDbColumn.name = pk.column_name;
                mysqlDbColumn.type = pk.data_type;
                mysqlDbColumn.comment = pk.column_comment;
                mysqlDbColumn.isNull = pk.is_nullable;
                mysqlDbColumn.strLen = pk.character_maximum_length.intValue();
                BeanField beanField = mysqlDbColumn.toBeanField();
                if (beanField == null) {
                    logger.warn("不支持[table:{}] [name:{}] [type:{}]类型数据库字段,忽略此字段!", config.tableName, mysqlDbColumn.name, mysqlDbColumn.type);
                }
                return beanField;
            }
            default-> {
                throw MyException.get("pk[{},{},{}] not support", pk.table_name, pk.column_name, pk.data_type);
            }
        }
    }

}
