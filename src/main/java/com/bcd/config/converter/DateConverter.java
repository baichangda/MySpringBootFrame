package com.bcd.config.convert;

import com.bcd.base.util.DateUtil;
import com.bcd.define.ErrorDefine;
import com.bcd.sys.util.ShiroUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Administrator on 2017/5/31.
 */
@Component
public class DateConvert implements Converter<String,Date> {
    @Override
    public Date convert(String source) {
        try {
            long t = Long.parseLong(source);
            return new Date(t);
        }catch (NumberFormatException e){
            try {
                String timeZone= ShiroUtil.getCurrentUser().getTimeZone();
                if(source.length()==DateUtil.DATE_FORMAT_DAY.length()){
                    return DateUtil.stringToDateWithUserTimeZone(timeZone,source,DateUtil.DATE_FORMAT_DAY);
                }else if(source.length()==DateUtil.DATE_FORMAT_SECOND.length()){
                    return DateUtil.stringToDateWithUserTimeZone(timeZone,source,DateUtil.DATE_FORMAT_SECOND);
                }else{
                    throw ErrorDefine.ERROR_DATECONVERT_FAILED.toRuntimeException();
                }
            } catch (Exception e1) {
                throw ErrorDefine.ERROR_DATECONVERT_FAILED.toRuntimeException();
            }
        }
    }

}
