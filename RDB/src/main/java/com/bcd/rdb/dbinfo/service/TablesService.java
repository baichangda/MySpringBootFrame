package com.bcd.rdb.dbinfo.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;


@Service
@SuppressWarnings("unchecked")
public abstract class TablesService {
    public abstract Workbook exportDBDesignerExcel(String dbName);
}
