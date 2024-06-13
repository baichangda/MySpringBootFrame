package com.bcd.base.support_jdbc.dbinfo.pgsql.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.bcd.base.exception.BaseException;
import com.bcd.base.support_jdbc.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.base.support_jdbc.dbinfo.pgsql.bean.TablesBean;
import com.bcd.base.support_jdbc.dbinfo.pgsql.util.DBInfoUtil;
import com.bcd.base.support_jdbc.dbinfo.service.DBService;
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

@ConditionalOnProperty(value = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
@Service
public class PgsqlDBServiceImpl implements DBService {
    private final String[] headArr = new String[]{"字段名", "数据类型", "能否为空", "默认值", "备注"};

    public static void main(String[] args) {
        PgsqlDBServiceImpl dbService = new PgsqlDBServiceImpl();
        dbService.exportDBDesignerExcelToDisk("127.0.0.1:5432", "root", "123qwe", "msbf", "/Users/baichangda/msbf.xlsx");
    }

    public void exportSpringDBDesignerExcel(String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getSpringConn()) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw BaseException.get(e);
        }
    }

    @Override
    public void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getConn(url, username, password, dbName)) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw BaseException.get(e);
        }
    }

    public void exportDBDesignerExcel(Connection connection, String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        List<List<Object>> dataList = new ArrayList<>();
        List<Object> emptyList = new ArrayList<>();
        for (int i = 0; i <= headArr.length - 1; i++) {
            emptyList.add("");
        }
        List<TablesBean> tablesList = DBInfoUtil.findTables(connection, dbName);
        for (TablesBean table : tablesList) {
            String tableName = table.table_name;
            //如果是flyway的版本信息表,则跳过
            if (tableName.equalsIgnoreCase("flyway_schema_history")) {
                continue;
            }
            String tableComment = table.table_comment;
            List<Object> define = new ArrayList<>();
            define.add(tableName + "(" + tableComment + ")");
            for (int i = 1; i <= headArr.length - define.size(); i++) {
                define.add("");
            }
            List<Object> head = new ArrayList<>(Arrays.asList(headArr));

            List<ColumnsBean> columnsList = DBInfoUtil.findColumns(
                    connection, dbName, tableName
            );

            dataList.add(define);
            dataList.add(head);
            columnsList.forEach(column -> {
                List<Object> data = new ArrayList<>();
                data.add(column.column_name);
                if (column.udt_name.equals("varchar")) {
                    data.add(column.udt_name + "(" + column.character_maximum_length + ")");
                } else {
                    data.add(column.udt_name);
                }
                data.add(column.is_nullable);
                data.add(column.column_default);
                data.add(column.description);
                dataList.add(data);
            });
            dataList.add(emptyList);
        }

        if (doBeforeWrite != null) {
            doBeforeWrite.run();
        }

        EasyExcel.write(os).sheet(0).registerWriteHandler(new SheetWriteHandler(){
            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                applyStyleToSheet(writeSheetHolder.getCachedSheet());
            }
        }).doWrite(dataList);
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
            throw BaseException.get(e);
        }
    }

}
