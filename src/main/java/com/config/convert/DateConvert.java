package com.config.convert;

import com.base.exception.BaseRuntimeException;
import com.base.i18n.I18NData;
import com.base.util.DateUtil;
import com.base.util.I18nUtil;
import com.sys.util.ShiroUtil;
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
                    throw BaseRuntimeException.getException(new I18NData("DateConvert.convert.FAILED"));
                }
            } catch (Exception e1) {
                throw BaseRuntimeException.getException(new I18NData("DateConvert.convert.FAILED"));
            }
        }
    }

}
