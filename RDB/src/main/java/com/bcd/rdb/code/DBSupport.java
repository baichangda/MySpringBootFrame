package com.bcd.rdb.code;

import com.bcd.rdb.code.data.BeanField;
import com.bcd.rdb.code.data.CodeConst;

import java.sql.Connection;
import java.util.List;

public interface DBSupport {

    Connection getSpringConn();

    String getDb();

    /**
     * 获取指定表的列名
     * @param config
     * @param connection
     * @return
     */
    List<BeanField> getTableBeanFieldList(TableConfig config, Connection connection);


    /**
     * 获取指定表的列名
     *
     * 支持如下几种主键类型
     * Byte、Short、Integer、Long、String
     *
     * @param config
     * @param connection
     * @return
     */
    CodeConst.PkType getTablePkType(TableConfig config,Connection connection);

}
