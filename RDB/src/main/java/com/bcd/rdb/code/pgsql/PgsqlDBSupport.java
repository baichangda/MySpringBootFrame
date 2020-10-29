package com.bcd.rdb.code.pgsql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.code.DBSupport;
import com.bcd.rdb.code.TableConfig;
import com.bcd.rdb.code.data.BeanField;
import com.bcd.rdb.code.data.CodeConst;
import com.bcd.rdb.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.rdb.dbinfo.pgsql.util.DBInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

public class PgsqlDBSupport implements DBSupport {

    Logger logger= LoggerFactory.getLogger(PgsqlDBSupport.class);

    @Override
    public Connection getSpringConn() {
        return DBInfoUtil.getSpringConn();
    }

    @Override
    public String getDb() {
        return DBInfoUtil.getDBProps().get("dbName").toString();
    }

    @Override
    public List<BeanField> getTableBeanFieldList(TableConfig config, Connection connection) {
        String tableName = config.getTableName();
        List<ColumnsBean> res = DBInfoUtil.findColumns(connection,config.getConfig().getDb(),tableName);
        return res.stream().map(e -> {
            PgsqlDBColumn pgsqlDBColumn = new PgsqlDBColumn();
            pgsqlDBColumn.setName(e.getColumn_name());
            pgsqlDBColumn.setType(e.getUdt_name());
            pgsqlDBColumn.setComment(e.getDescription());
            pgsqlDBColumn.setIsNull(e.getIs_nullable());
            pgsqlDBColumn.setStrLen(e.getCharacter_maximum_length());
            BeanField beanField= pgsqlDBColumn.toBeanField();
            if(beanField==null){
                logger.info("不支持[table:{}] [name:{}] [type:{}]类型数据库字段,忽略此字段!",config.getTableName(),pgsqlDBColumn.getName(),pgsqlDBColumn.getType());
            }
            return beanField;
        }).filter(e->{
            if(e==null){
                return false;
            }
            if("id".equals(e.getName())){
                return false;
            }else {
                if (config.isNeedCreateInfo()) {
                    if (CodeConst.CREATE_INFO_FIELD_NAME.contains(e.getName())) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }).collect(Collectors.toList());
    }

    @Override
    public CodeConst.PkType getTablePkType(TableConfig config, Connection connection) {
        ColumnsBean pk= DBInfoUtil.findPKColumn(connection,config.getConfig().getDb(),config.getTableName());
        switch (pk.getUdt_name()){
            case "int2":{
                return CodeConst.PkType.Integer;
            }
            case "int4":{
                return CodeConst.PkType.Integer;
            }
            case "int8":{
                return CodeConst.PkType.Long;
            }
            case "varchar":{
                return CodeConst.PkType.String;
            }
            default:{
                throw BaseRuntimeException.getException("pk[{0},{1},{2}] not support",pk.getTable_name(),pk.getColumn_name(),pk.getUdt_name());
            }
        }
    }
}
