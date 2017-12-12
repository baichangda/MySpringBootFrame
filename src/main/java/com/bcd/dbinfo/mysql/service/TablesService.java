package com.bcd.dbinfo.mysql.service;

import com.alibaba.fastjson.JSONArray;
import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.util.BeanUtil;
import com.bcd.base.util.ExcelUtil;
import com.bcd.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.dbinfo.mysql.bean.TablesBean;
import com.bcd.dbinfo.mysql.repository.TablesRepository;
import com.bcd.rdb.util.ConditionUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TablesService {

    private String [] headArr=new String[]{"字段名","数据类型","不能为空","默认值","备注"};
    private String [] columnArr=new String[]{"columnName","dataType","isNullable","columnDefault","columnComment"};
    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private ColumnsService columnsService ;

    public List<TablesBean> list(Condition condition){
        return tablesRepository.findAll(ConditionUtil.toSpecification(condition));
    }

    public HSSFWorkbook exportDBDesigner(String dbName){
        List<List> dataList=new ArrayList<>();
        List emptyList=new ArrayList();
        for (String s : headArr) {
            emptyList.add("");
        }
        List<TablesBean> tablesList= list(new StringCondition("tableSchema",dbName));
        tablesList.forEach(table->{
            List define=new ArrayList();
            List head=new ArrayList();
            List data=new ArrayList();
            define.add(table.getTableName());
            for(int i=1;i<=headArr.length-define.size();i++){
                define.add("");
            }
            head.add(Arrays.asList(headArr));

            List<ColumnsBean> columnsList= columnsService.list(
                    Condition.and(
                            new StringCondition("tableSchema",dbName),
                            new StringCondition("tableName",table.getTableName())
                    )
            );

            columnsList.forEach(column->{
                for (String columnName : columnArr) {
                    data.add(BeanUtil.getFieldVal(column,columnName));
                }
            });
            dataList.add(define);
            dataList.add(head);
            dataList.add(data);
            dataList.add(emptyList);
        });
        return ExcelUtil.exportExcel(dataList);
    }
}
