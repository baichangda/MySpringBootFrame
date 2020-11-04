package com.bcd.rdb.dbinfo.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;


@Service
@SuppressWarnings("unchecked")
public abstract class TablesService {
    public abstract void exportDBDesignerExcel(String dbName, OutputStream os);
}
