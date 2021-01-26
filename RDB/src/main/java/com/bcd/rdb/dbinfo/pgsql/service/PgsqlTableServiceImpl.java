package com.bcd.rdb.dbinfo.pgsql.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.FileUtil;
import com.bcd.rdb.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.rdb.dbinfo.pgsql.bean.TablesBean;
import com.bcd.rdb.dbinfo.pgsql.util.DBInfoUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import org.apache.poi.ss.usermodel.*;
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
@ConditionalOnProperty(value = "spring.datasource.driver-class-name",havingValue ="org.postgresql.Driver")
@Service
public class PgsqlTableServiceImpl implements TablesService {
    private String[] headArr = new String[]{"字段名", "数据类型", "能否为空", "默认值", "备注"};

    public void exportSpringDBDesignerExcel(String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException {
        try(Connection connection= DBInfoUtil.getSpringConn()){
            exportDBDesignerExcel(connection,dbName,os,doBeforeWrite);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    @Override
    public void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException {
        try(Connection connection= DBInfoUtil.getConn(url,username,password,dbName)){
            exportDBDesignerExcel(connection,dbName,os,doBeforeWrite);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public void exportDBDesignerExcel(Connection connection,String dbName,OutputStream os,Runnable doBeforeWrite){
        List<List> dataList = new ArrayList<>();
        List emptyList = new ArrayList();
        for (int i = 0; i <= headArr.length - 1; i++) {
            emptyList.add("");
        }
        List<TablesBean> tablesList = DBInfoUtil.findTables(connection,dbName);
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
                    connection,dbName, tableName
            );

            dataList.add(define);
            dataList.add(head);
            columnsList.forEach(column -> {
                List data = new ArrayList();
                data.add(column.getColumn_name());
                data.add(column.getUdt_name());
                data.add(column.getIs_nullable());
                data.add(column.getColumn_default());
                data.add(column.getDescription());
                dataList.add(data);
            });
            dataList.add(emptyList);
        }

        if(doBeforeWrite!=null){
            doBeforeWrite.run();
        }

        EasyExcel.write(os)
                .excelType(ExcelTypeEnum.XLSX)
                .registerWriteHandler(workbookWriteHandler)
                .sheet(dbName).doWrite(dataList);
    }

    /**
     * 导出数据库设计到本地
     * @param url
     * @param username
     * @param password
     * @param dbName
     * @param file 如果不存在,则创建
     */
    public void exportDBDesignerExcelToDisk(String url,String username,String password,String dbName,String file){
        Path p= Paths.get(file);
        FileUtil.createFileIfNotExists(p);
        try(OutputStream os=Files.newOutputStream(p);
            Connection connection=DBInfoUtil.getConn(url, username, password,dbName)){
            exportDBDesignerExcel(connection,dbName,os,null);
        }catch (IOException|SQLException e){
            throw BaseRuntimeException.getException(e);
        }
    }

    class MyCellWriteHandler implements CellWriteHandler {
        CellStyle cellStyle1;
        CellStyle cellStyle2;

        @Override
        public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {

        }

        @Override
        public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

        }

        @Override
        public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, CellData cellData, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

        }

        @Override
        public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
            int y=cell.getColumnIndex();
            if(y==0){
                //设置标头列样式
                if(cellStyle1==null){
                    cellStyle1=cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    cellStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    cellStyle1.setBorderLeft(BorderStyle.THIN);
                    cellStyle1.setBorderBottom(BorderStyle.THIN);
                    cellStyle1.setBorderTop(BorderStyle.THIN);
                    cellStyle1.setBorderRight(BorderStyle.THIN);
                    cellStyle1.setWrapText(true);
                    cellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
                }
                cell.setCellStyle(cellStyle1);
            }else{
                //设置内容列样式
                if(cellStyle2==null){
                    cellStyle2=cell.getRow().getSheet().getWorkbook().createCellStyle();
                    cellStyle2.setBorderLeft(BorderStyle.THIN);
                    cellStyle2.setBorderBottom(BorderStyle.THIN);
                    cellStyle2.setBorderTop(BorderStyle.THIN);
                    cellStyle2.setBorderRight(BorderStyle.THIN);
                    cellStyle2.setWrapText(true);

                }
                cell.setCellStyle(cellStyle2);
            }
        }
    }

    public static void main(String[] args) {
        PgsqlTableServiceImpl tableService=new PgsqlTableServiceImpl();
        tableService.exportDBDesignerExcelToDisk("db.hbluewhale.com:12921","dbuser","hlxpassword","test_bcd","d:\\msbf.xlsx");
    }
}
