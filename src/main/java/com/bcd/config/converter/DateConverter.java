package com.bcd.config.converter;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.DateUtil;
import com.bcd.define.MessageDefine;
import com.bcd.sys.shiro.ShiroUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/31.
 */
@Component
public class DateConverter implements Converter<String,Date> {
    @Override
    public Date convert(String source) {
        if(StringUtils.isEmpty(source)){
            return null;
        }
        try {
            long t = Long.parseLong(source);
            return new Date(t);
        }catch (NumberFormatException e){
            try {
                String offsetId= ShiroUtil.getCurrentUser().getOffsetId();
                ZoneOffset zoneOffset= ZoneOffset.of(offsetId);
                int sourceLen=source.length();
                if(sourceLen==DateUtil.PARAM_DATE_FORMAT_DAY.length()){
                    return DateUtil.stringToDate(source,DateUtil.PARAM_DATE_FORMAT_DAY,zoneOffset);
                }else if(sourceLen==DateUtil.PARAM_DATE_FORMAT_SECOND.length()){
                    return DateUtil.stringToDate(source,DateUtil.PARAM_DATE_FORMAT_SECOND,zoneOffset);
                }else{
                    throw MessageDefine.ERROR_DATE_CONVERT_FAILED.toRuntimeException(source);
                }
            } catch (Exception e1) {
                throw BaseRuntimeException.getException(e1);
            }
        }
    }

}
