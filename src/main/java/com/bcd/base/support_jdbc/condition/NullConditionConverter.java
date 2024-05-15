package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.util.Collections;

/**
 * Created by Administrator on 2017/9/15.
 */
public class NullConditionConverter implements Converter<NullCondition, ConvertRes> {
    @Override
    public ConvertRes convert(NullCondition condition, Object... exts) {
        final String fieldName = condition.fieldName;
        final NullCondition.Handler handler = condition.handler;
        final BeanInfo<?> beanInfo = (BeanInfo<?>) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        return switch (handler) {
            case NULL -> new ConvertRes(columnName + " is null", Collections.emptyList());
            case NOT_NULL -> new ConvertRes(columnName + " is not null", Collections.emptyList());
        };
    }
}
