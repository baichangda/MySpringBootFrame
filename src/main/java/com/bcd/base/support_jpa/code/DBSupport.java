package com.bcd.base.support_jpa.code;

import com.bcd.base.support_jpa.code.data.BeanField;
import com.bcd.base.support_jpa.dbinfo.data.DBInfo;

import java.sql.Connection;
import java.util.List;

public interface DBSupport {

    DBInfo getSpringDBConfig();


    /**
     * 获取指定表的列名
     *
     * @param config
     * @param connection
     * @return
     */
    List<BeanField> getTableBeanFieldList(TableConfig config, Connection connection);


    /**
     * 获取指定表的列名
     * <p>
     * 支持如下几种主键类型
     * Byte、Short、Integer、Long、String
     *
     * @param config
     * @param connection
     * @return
     */
    CodeConst.PkType getTablePkType(TableConfig config, Connection connection);


}


