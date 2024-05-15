package com.bcd.base.support_jdbc.dbinfo.service;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


@Service
public interface DBService {
    /**
     * 导出application.yml中配置的url中指定数据库配置
     *
     * @param dbName
     * @param os
     * @param doBeforeWrite
     * @throws IOException
     */
    void exportSpringDBDesignerExcel(String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException;

    /**
     * 导出指定数据库配置
     *
     * @param url
     * @param username
     * @param password
     * @param dbName
     * @param os
     * @param doBeforeWrite
     * @throws IOException
     */
    void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os, Runnable doBeforeWrite) throws IOException;

    default void applyStyleToSheet(Sheet sheet){
        sheet.setAutobreaks(true);
        sheet.setColumnWidth(0, 256 * 45 + 184);
        sheet.setColumnWidth(1, 256 * 15 + 184);
        sheet.setColumnWidth(2, 256 * 15 + 184);
        sheet.setColumnWidth(3, 256 * 30 + 184);
        sheet.setColumnWidth(4, 256 * 100 + 184);
    }

}
