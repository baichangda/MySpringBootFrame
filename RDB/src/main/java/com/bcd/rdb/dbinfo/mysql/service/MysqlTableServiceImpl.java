package com.bcd.rdb.dbinfo.mysql.service;

import com.bcd.base.util.ExcelUtil;
import com.bcd.rdb.dbinfo.mysql.util.DBInfoUtil;
import com.bcd.rdb.dbinfo.service.TablesService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Service
public class MysqlTableServiceImpl extends TablesService {
    private String [] headArr=new String[]{"字段名","数据类型","不能为空","默认值","备注"};
    public Workbook exportDBDesigner(String dbName){
        List<List> dataList=new ArrayList<>();
        List emptyList=new ArrayList();
        for (int i=0;i<=headArr.length-1;i++) {
            emptyList.add("");
        }
        List<Map<String,Object>> tablesList= DBInfoUtil.findTables(dbName);
        for (Map<String,Object> table : tablesList) {
            String tableName=table.get("TABLE_NAME").toString();
            //如果是flyway的版本信息表,则跳过
            if(tableName.equalsIgnoreCase("flyway_schema_history")){
                continue;
            }
            String tableComment=table.get("TABLE_COMMENT").toString();
            List define=new ArrayList();
            List head=new ArrayList();
            define.add(tableName+"("+tableComment+")");
            for(int i=1;i<=headArr.length-define.size();i++){
                define.add("");
            }
            head.addAll(Arrays.asList(headArr));

            List<Map<String,Object>> columnsList= null;
            columnsList = DBInfoUtil.findColumns(
                    dbName,tableName
            );

            dataList.add(define);
            dataList.add(head);
            columnsList.forEach(column->{
                List data=new ArrayList();
                data.add(column.get("COLUMN_NAME"));
                data.add(column.get("DATA_TYPE"));
                data.add(column.get("IS_NULLABLE"));
                data.add(column.get("COLUMN_DEFAULT"));
                data.add(column.get("COLUMN_COMMENT"));
                dataList.add(data);
            });
            dataList.add(emptyList);
        }
        return ExcelUtil.exportExcel_2007(dataList,null);
    }
}
