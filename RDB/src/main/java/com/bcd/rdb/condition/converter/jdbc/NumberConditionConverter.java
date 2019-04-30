package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.util.RDBUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter  implements Converter<NumberCondition,String> {
    @Override
    public String convert(NumberCondition condition, Object... exts) {
        StringBuilder where=new StringBuilder();
        NumberCondition.Handler handler= condition.handler;
        Object val=condition.val;
        String columnName= StringUtil.toFirstSplitWithUpperCase(condition.fieldName,'_');
        Map<String,Object> paramMap=(Map<String,Object>)exts[0];
        String paramName= RDBUtil.generateRandomParamName(columnName,paramMap);
        if(val!=null) {
            switch (handler) {
                case EQUAL: {
                    where.append(columnName);
                    where.append(" = ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case LT: {
                    where.append(columnName);
                    where.append(" < ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case LE: {
                    where.append(columnName);
                    where.append(" <= ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case GT: {
                    where.append(columnName);
                    where.append(" > ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case GE: {
                    where.append(columnName);
                    where.append(" >= ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case NOT_EQUAL: {
                    where.append(columnName);
                    where.append(" <> ");
                    where.append(":"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case IN: {
                    where.append(columnName);
                    where.append(" IN ");
                    where.append("(:"+paramName+")");
                    paramMap.put(paramName,val);
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else if(val.getClass().isArray()){
                        List notEmptyList= Arrays.asList(val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    where.append(columnName);
                    where.append(" in ");
                    where.append("(:"+paramName+")");
                    paramMap.put(paramName,val);
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Do Not Support ["+handler+"]!");
                }
            }
        }
        return where.length()==0?null:where.toString();
    }
}
