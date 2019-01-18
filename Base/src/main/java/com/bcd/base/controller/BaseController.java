package com.bcd.base.controller;

import com.bcd.base.cache.anno.MyCacheClass;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.base.util.FileUtil;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/11.
 */
@MyCacheClass
public class BaseController {

    public final static String DEFAULT_RESPONSE_ENCODING="UTF-8";

    /**
     * 下载文件流
     * @param is 文件流
     * @param fileName 导出的文件名
     * @param response 响应response
     */
    protected void response(InputStream is, String fileName, HttpServletResponse response){
        try {
            response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(DEFAULT_RESPONSE_ENCODING), "ISO-8859-1"));
            try(OutputStream os=response.getOutputStream()){
                FileUtil.write(is,os);
            }
        }catch (IOException e){
            throw BaseRuntimeException.getException(e);
        }
    }


    /**
     * 下载文件
     * @param path 文件
     * @param fileName 导出的文件名
     * @param response 响应response
     */
    protected void response(Path path, String fileName, HttpServletResponse response){
        try {
            response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(DEFAULT_RESPONSE_ENCODING), "ISO-8859-1"));
            try(InputStream is= Files.newInputStream(path);
                OutputStream os=response.getOutputStream()){
                FileUtil.write(is,os);
            }
        }catch (IOException e){
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 导出excel
     * @param workbook excel
     * @param fileName 导出的文件名
     * @param response 响应response
     */
    protected void response(Workbook workbook, String fileName, HttpServletResponse response){
        try {
            response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(DEFAULT_RESPONSE_ENCODING), "ISO-8859-1"));
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }catch (IOException e){
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 文件名字带上 '-时间数字'
     * 例如:
     * name.xlsx
     * name-2018111111235959.xlsx
     * @param fileName
     * @return
     */
    protected String toDateFileName(String fileName){
        int index=fileName.lastIndexOf('.');
        long dateNum= DateZoneUtil.getDateNum(new Date(), ChronoUnit.MILLIS);
        if(index==-1){
            return fileName +"-"+ dateNum;
        }else {
            return fileName.substring(0,index)+"-"+dateNum+fileName.substring(index);
        }
    }
}
