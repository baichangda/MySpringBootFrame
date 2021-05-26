package com.bcd.base.rdb.dbinfo.pgsql.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.rdb.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.base.rdb.dbinfo.pgsql.bean.TablesBean;
import com.bcd.base.rdb.dbinfo.pgsql.util.DBInfoUtil;
import com.bcd.base.rdb.dbinfo.service.DBService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
@ConditionalOnProperty(value = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
@Service
public class PgsqlDBServiceImpl implements DBService {
    private String[] headArr = new String[]{"字段名", "数据类型", "能否为空", "默认值", "备注"};

    public static void main(String[] args) {
        PgsqlDBServiceImpl dbService = new PgsqlDBServiceImpl();
        dbService.exportDBDesignerExcelToDisk("127.0.0.1:5432", "root", "123qwe", "msbf", "/Users/baichangda/msbf.xlsx");
    }

    public void exportSpringDBDesignerExcel(String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getSpringConn()) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    @Override
    public void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getConn(url, username, password, dbName)) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public void exportDBDesignerExcel(Connection connection, String dbName, OutputStream os, Runnable doBeforeWrite) {
        List<List> dataList = new ArrayList<>();
        List emptyList = new ArrayList();
        for (int i = 0; i <= headArr.length - 1; i++) {
            emptyList.add("");
        }
        List<TablesBean> tablesList = DBInfoUtil.findTables(connection, dbName);
        for (TablesBean table : tablesList) {
            String tableName = table.getTable_name();
            //如果是flyway的版本信息表,则跳过
            if (tableName.equalsIgnoreCase("flyway_schema_history")) {
                continue;
            }
            String tableComment = table.getTable_comment();
            List define = new ArrayList();
            List head = new ArrayList();
            define.add(tableName + "(" + tableComment + ")");
            for (int i = 1; i <= headArr.length - define.size(); i++) {
                define.add("");
            }
            head.addAll(Arrays.asList(headArr));

            List<ColumnsBean> columnsList = DBInfoUtil.findColumns(
                    connection, dbName, tableName
            );

            dataList.add(define);
            dataList.add(head);
            columnsList.forEach(column -> {
                List data = new ArrayList();
                data.add(column.getColumn_name());
                if (column.getUdt_name().equals("varchar")) {
                    data.add(column.getUdt_name() + "(" + column.getCharacter_maximum_length() + ")");
                } else {
                    data.add(column.getUdt_name());
                }
                data.add(column.getIs_nullable());
                data.add(column.getColumn_default());
                data.add(column.getDescription());
                dataList.add(data);
            });
            dataList.add(emptyList);
        }

        if (doBeforeWrite != null) {
            doBeforeWrite.run();
        }

        EasyExcel.write(os)
                .excelType(ExcelTypeEnum.XLSX)
                .registerWriteHandler(workbookWriteHandler)
                .sheet(dbName).doWrite(dataList);
    }

    /**
     * 导出数据库设计到本地
     *
     * @param url
     * @param username
     * @param password
     * @param dbName
     * @param file     如果不存在,则创建
     */
    public void exportDBDesignerExcelToDisk(String url, String username, String password, String dbName, String file) {
        Path p = Paths.get(file);
        try (OutputStream os = Files.newOutputStream(p);
             Connection connection = DBInfoUtil.getConn(url, username, password, dbName)) {
            exportDBDesignerExcel(connection, dbName, os, null);
        } catch (IOException | SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

}
