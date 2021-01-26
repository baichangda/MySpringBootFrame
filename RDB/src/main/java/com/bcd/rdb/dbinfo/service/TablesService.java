package com.bcd.rdb.dbinfo.service;

import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


@Service
@SuppressWarnings("unchecked")
public interface TablesService {
    /**
     * 导出application.yml中配置的url中指定数据库配置
     * @param dbName
     * @param os
     * @param doBeforeWrite
     * @throws IOException
     */
    void exportSpringDBDesignerExcel(String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException;


    /**
     * 导出指定数据库配置
     * @param url
     * @param username
     * @param password
     * @param dbName
     * @param os
     * @param doBeforeWrite
     * @throws IOException
     */
    void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException;

    MyWorkbookWriteHandler workbookWriteHandler=new MyWorkbookWriteHandler();

    class MyWorkbookWriteHandler implements WorkbookWriteHandler {
        @Override
        public void beforeWorkbookCreate() {

        }

        @Override
        public void afterWorkbookCreate(WriteWorkbookHolder writeWorkbookHolder) {

        }

        @Override
        public void afterWorkbookDispose(WriteWorkbookHolder writeWorkbookHolder) {
            int sheetNum= writeWorkbookHolder.getCachedWorkbook().getNumberOfSheets();
            for (int i = 0; i < sheetNum; i++) {
                SXSSFSheet sheet= (SXSSFSheet)writeWorkbookHolder.getCachedWorkbook().getSheetAt(i);
                sheet.trackAllColumnsForAutoSizing();
                sheet.setColumnWidth(0,256*15+184);
                sheet.setColumnWidth(1,256*15+184);
                sheet.setColumnWidth(2,256*15+184);
                sheet.setColumnWidth(3,256*30+184);
                sheet.setColumnWidth(4,256*100+184);
            }
        }
    }
}
