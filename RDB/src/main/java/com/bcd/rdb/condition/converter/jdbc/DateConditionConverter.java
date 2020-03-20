package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.util.RDBUtil;

import java.util.Map;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class DateConditionConverter  implements Converter<DateCondition,String> {
    @Override
    public String convert(DateCondition condition, Object... exts) {
        StringBuilder where=new StringBuilder();
        DateCondition.Handler handler= condition.handler;
        Object val=condition.val;
        String columnName= StringUtil.toFirstSplitWithUpperCase(condition.fieldName,'_');
        Map<String,Object> paramMap=(Map<String,Object>)exts[0];
        Map<String,Integer> paramToCount=(Map<String,Integer>)exts[1];
        String paramName= RDBUtil.generateRandomParamName(columnName,paramToCount);
        if(val!=null) {
            switch (handler) {
                case EQUAL: {
                    where.append(columnName);
                    where.append(" = ");
                    where.append(":");
                    where.append(paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case LE: {
                    where.append(columnName);
                    where.append(" <= ");
                    where.append(":");
                    where.append(paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case LT: {
                    where.append(columnName);
                    where.append(" < ");
                    where.append(":");
                    where.append(paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case GE: {
                    where.append(columnName);
                    where.append(" >= ");
                    where.append(":");
                    where.append(paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case GT: {
                    where.append(columnName);
                    where.append(" > ");
                    where.append(":");
                    where.append(paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                default :{
                    throw BaseRuntimeException.getException("[DateConditionConverter.convert],Do Not Support ["+handler+"]!");
                }
            }
        }
        return where.length()==0?null:where.toString();
    }
}
