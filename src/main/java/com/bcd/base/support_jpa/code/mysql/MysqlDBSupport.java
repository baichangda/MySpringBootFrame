package com.bcd.base.support_jpa.code.mysql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.code.CodeConst;
import com.bcd.base.support_jpa.code.DBSupport;
import com.bcd.base.support_jpa.code.TableConfig;
import com.bcd.base.support_jpa.code.data.BeanField;
import com.bcd.base.support_jpa.dbinfo.data.DBInfo;
import com.bcd.base.support_jpa.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.base.support_jpa.dbinfo.mysql.util.DBInfoUtil;
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
        String tableName = config.getTableName();
        List<ColumnsBean> res = DBInfoUtil.findColumns(connection, config.getConfig().getDbInfo().getDb(), tableName);
        return res.stream().map(e -> {
            MysqlDBColumn mysqlDbColumn = new MysqlDBColumn();
            mysqlDbColumn.setName(e.getColumn_name());
            mysqlDbColumn.setType(e.getData_type());
            mysqlDbColumn.setComment(e.getColumn_comment());
            mysqlDbColumn.setIsNull(e.getIs_nullable());
            mysqlDbColumn.setStrLen(e.getCharacter_maximum_length().intValue());
            BeanField beanField = mysqlDbColumn.toBeanField();
            if (beanField == null) {
                logger.warn("不支持[table:{}] [name:{}] [type:{}]类型数据库字段,忽略此字段!", config.getTableName(), mysqlDbColumn.getName(), mysqlDbColumn.getType());
            }
            return beanField;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public CodeConst.PkType getTablePkType(TableConfig config, Connection connection) {
        ColumnsBean pk = DBInfoUtil.findPKColumn(connection, config.getConfig().getDbInfo().getDb(), config.getTableName());
        switch (pk.getData_type()) {
            case "int": {
                return CodeConst.PkType.Integer;
            }
            case "bigint": {
                return CodeConst.PkType.Long;
            }
            case "varchar": {
                return CodeConst.PkType.String;
            }
            default: {
                throw BaseRuntimeException.getException("pk[{0},{1},{2}] not support", pk.getTable_name(), pk.getColumn_name(), pk.getData_type());
            }
        }
    }
}
