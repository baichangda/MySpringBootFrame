package com.bcd.base.support_jdbc.dbinfo.mysql.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.base.support_jdbc.dbinfo.mysql.bean.TablesBean;
import com.bcd.base.support_jdbc.dbinfo.mysql.util.DBInfoUtil;
import com.bcd.base.support_jdbc.dbinfo.service.DBService;
import org.apache.poi.ss.usermodel.Cell;
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
@ConditionalOnProperty(value = "spring.datasource.driver-class-name", havingValue = "com.mysql.cj.jdbc.Driver")
@Service
public class MysqlDBServiceImpl implements DBService {
    private final String[] headArr = new String[]{"字段名", "数据类型", "能否为空", "默认值", "备注"};

    public static void main(String[] args) {
        MysqlDBServiceImpl tableService = new MysqlDBServiceImpl();
        tableService.exportDBDesignerExcelToDisk("192.168.23.129", "root", "bcd", "bcd", "d:/db-bcd.xlsx");

    }

    public void exportSpringDBDesignerExcel(String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getSpringConn()) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw MyException.get(e);
        }
    }

    @Override
    public void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException {
        try (Connection connection = DBInfoUtil.getConn(url, username, password)) {
            exportDBDesignerExcel(connection, dbName, os, doBeforeWrite);
        } catch (SQLException e) {
            throw MyException.get(e);
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
                data.add(column.column_type);
                data.add(column.is_nullable);
                data.add(column.column_default);
                data.add(column.column_comment);
                dataList.add(data);
            });
            dataList.add(emptyList);
        }

        if (doBeforeWrite != null) {
            doBeforeWrite.run();
        }

        EasyExcel.write(os).sheet(0).registerWriteHandler(new SheetWriteHandler() {
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
             Connection connection = DBInfoUtil.getConn(url, username, password)) {
            exportDBDesignerExcel(connection, dbName, os, null);
        } catch (IOException | SQLException e) {
            throw MyException.get(e);
        }
    }
}
