package com.bcd.base.controller;

import com.bcd.base.cache.anno.MyCacheClass;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.base.util.FileUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/11.
 */
@MyCacheClass
public class BaseController {

    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    public final static String DEFAULT_RESPONSE_ENCODING = "UTF-8";

    /**
     * 响应文件流之前设置response
     * @param fileName
     * @param response
     * @throws UnsupportedEncodingException
     */
    protected void doBeforeResponseFile(String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(DEFAULT_RESPONSE_ENCODING), StandardCharsets.ISO_8859_1));
    }


    /**
     * 文件名字带上 '-时间数字'
     * 例如:
     * name.xlsx
     * name-20181111112359.xlsx
     *
     * @param fileName
     * @return
     */
    protected String toDateFileName(String fileName) {
        int index = fileName.lastIndexOf('.');
        long dateNum = DateZoneUtil.getDateNum(new Date(), ChronoUnit.SECONDS);
        if (index == -1) {
            return fileName + "-" + dateNum;
        } else {
            return fileName.substring(0, index) + "-" + dateNum + fileName.substring(index);
        }
    }
}
