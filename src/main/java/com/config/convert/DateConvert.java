package com.config.convert;

import com.base.util.DateUtil;
import com.base.util.I18nUtil;
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
            //将浏览器时区时间转换成当前时区时间
//            t=DateUtil.transferGMTToLocalTimeZone(t, ShiroUtil.getCurrentUser().getTimeZoneOffsetGMT());
            return new Date(t);
        }catch (NumberFormatException e){
            try {
                if(source.length()==DateUtil.DATE_FORMAT_DAY.length()){
                    return DateUtil.stringToDate(source,DateUtil.DATE_FORMAT_DAY);
                }else if(source.length()==DateUtil.DATE_FORMAT_SECOND.length()){
                    return DateUtil.stringToDate(source,DateUtil.DATE_FORMAT_SECOND);
                }else{
                    throw new RuntimeException();
                }
            } catch (Exception e1) {
                throw new RuntimeException(I18nUtil.getMessage("DateConvert.convert.FAILED"));
            }
        }
    }

}
