package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NullConditionConverter implements Converter<NullCondition,String> {
    @Override
    public String convert(NullCondition condition, Object... exts) {
        StringBuilder where=new StringBuilder();
        NullCondition.Handler handler= condition.handler;
        Object val=condition.val;
        String columnName= StringUtil.toFirstSplitWithUpperCase(condition.fieldName,'_');
        switch (handler) {
            case NULL: {
                where.append(columnName);
                where.append(" IS NULL");
                break;
            }
            case NOT_NULL: {
                where.append(columnName);
                where.append(" IS NOT NULL");
                break;
            }
            default :{
                throw BaseRuntimeException.getException("[NullConditionConverter.convert],Do Not Support ["+handler+"]!");
            }
        }
        return where.length()==0?null:where.toString();
    }
}
