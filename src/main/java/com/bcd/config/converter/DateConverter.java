package com.bcd.config.converter;

import com.bcd.base.util.DateUtil;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.define.MessageDefine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 设别如下几种date参数
 * 1、毫秒时间戳、example: source=1611629450000
 * 2、日期类型字符串
 *  此时日期格式有两种{@link DateUtil#DATE_FORMAT_DAY}、{@link DateUtil#DATE_FORMAT_SECOND}
 *  字符串必须以s开头
 *  example: s20210126、s20210126111111
 *
 *
 */
@Component
public class DateConverter implements Converter<String,Date> {

    @Override
    public Date convert(String source) {
        if(StringUtils.isEmpty(source)){
            return null;
        }else{
            char first=source.charAt(0);
            if(first=='s'){
                String val=source.substring(1);
                int sourceLen=val.length();
                if(sourceLen==DateUtil.DATE_FORMAT_DAY.length()){
                    return DateZoneUtil.stringToDate_day(val);
                }else if(sourceLen==DateUtil.DATE_FORMAT_SECOND.length()){
                    return DateZoneUtil.stringToDate_second(val);
                }else{
                    throw MessageDefine.ERROR_DATE_CONVERT_FAILED.toRuntimeException(source);
                }
            }else{
                long t = Long.parseLong(source);
                return new Date(t);
            }
        }
    }

}
