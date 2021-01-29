package com.bcd.base.controller;

import com.bcd.base.cache.anno.MyCacheClass;
import com.bcd.base.util.DateZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/11.
 */
@MyCacheClass
public class BaseController {

    protected Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * 响应文件流之前设置response
     * @param fileName
     * @param response
     */
    protected void doBeforeResponseFile(String fileName, HttpServletResponse response){
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
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
        long dateNum = Long.parseLong(DateZoneUtil.dateToString_second(new Date()));
        if (index == -1) {
            return fileName + "-" + dateNum;
        } else {
            return fileName.substring(0, index) + "-" + dateNum + fileName.substring(index);
        }
    }

}
