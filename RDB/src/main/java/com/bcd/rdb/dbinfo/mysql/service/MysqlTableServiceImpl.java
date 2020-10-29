package com.bcd.rdb.dbinfo.mysql.service;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExcelUtil;
import com.bcd.base.util.FileUtil;
import com.bcd.rdb.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.rdb.dbinfo.mysql.bean.TablesBean;
import com.bcd.rdb.dbinfo.mysql.util.DBInfoUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;

@SuppressWarnings("unchecked")
@Service
public class MysqlTableServiceImpl extends TablesService {
    private String[] headArr = new String[]{"字段名", "数据类型", "能否为空", "默认值", "备注"};

    public Workbook exportDBDesignerExcel(String dbName) {
        try(Connection connection=DBInfoUtil.getSpringConn()){
            return exportDBDesignerExcel(connection,dbName);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public Workbook exportDBDesignerExcel(Connection connection,String dbName) {
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
                data.add(column.getColumn_type());
                data.add(column.getData_type());
                data.add(column.getIs_nullable());
                data.add(column.getColumn_default());
                data.add(column.getColumn_comment());
                dataList.add(data);
            });
            dataList.add(emptyList);
        }
        return ExcelUtil.exportExcel_2007(dataList, null);
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
        Workbook workbook;
        try(Connection connection=DBInfoUtil.getConn(url, username, password)){
            workbook= exportDBDesignerExcel(connection,dbName);
        }catch (SQLException e){
            throw BaseRuntimeException.getException(e);
        }
        Path p= Paths.get(file);
        FileUtil.createFileIfNotExists(p);
        try(OutputStream os=Files.newOutputStream(p)){
            workbook.write(os);
        }catch (IOException e){
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void main(String[] args) {
        MysqlTableServiceImpl mysqlTableService=new MysqlTableServiceImpl();
        mysqlTableService.exportDBDesignerExcelToDisk("127.0.0.1:3306","root","123456","msbf","/Users/baichangda/msbf.xlsx");
    }
}
