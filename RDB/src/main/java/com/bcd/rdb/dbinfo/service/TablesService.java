package com.bcd.rdb.dbinfo.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;


@Service
@SuppressWarnings("unchecked")
public abstract class TablesService {
    /**
     * 导出application.yml中配置的url中指定数据库配置
     * @param dbName
     * @param os
     * @param doBeforeWrite
     * @throws IOException
     */
    public abstract void exportSpringDBDesignerExcel(String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException;


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
    public abstract void exportDBDesignerExcel(String url, String username, String password, String dbName, OutputStream os,Runnable doBeforeWrite) throws IOException;
}
