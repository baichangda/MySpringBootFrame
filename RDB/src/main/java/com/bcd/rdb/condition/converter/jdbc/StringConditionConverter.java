package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.util.RDBUtil;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class StringConditionConverter implements Converter<StringCondition,String> {
    @Override
    public String convert(StringCondition condition, Object... exts) {
        StringBuilder where=new StringBuilder();
        StringCondition.Handler handler= condition.handler;
        Object val=condition.val;
        String columnName= StringUtil.toFirstSplitWithUpperCase(condition.fieldName,'_');
        Map<String,Object> paramMap=(Map<String,Object>)exts[0];
        String paramName= RDBUtil.generateRandomParamName(columnName,paramMap);
        if(!StringUtils.isEmpty(val)){
            switch (handler){
                case EQUAL: {
                    where.append(columnName);
                    where.append(" = ");
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
                case ALL_LIKE: {
                    where.append(columnName);
                    where.append(" LIKE ");
                    where.append("%:"+paramName+"%");
                    paramMap.put(paramName,val);
                    break;
                }
                case LEFT_LIKE: {
                    where.append(columnName);
                    where.append(" LIKE ");
                    where.append("%:"+paramName);
                    paramMap.put(paramName,val);
                    break;
                }
                case RIGHT_LIKE: {
                    where.append(columnName);
                    where.append(" LIKE ");
                    where.append(":"+paramName+"%");
                    paramMap.put(paramName,val);
                    break;
                }
                case IN: {
                    where.append(columnName);
                    where.append(" IN ");
                    where.append("(:"+paramName+")");
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else if(val.getClass().isArray()){
                        List notEmptyList= Arrays.asList(val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    where.append(columnName);
                    where.append(" not in ");
                    where.append("(:"+paramName+")");
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        paramMap.put(paramName,notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                default :{
                    throw BaseRuntimeException.getException("[StringConditionConverter.convert],Do Not Support ["+handler+"]!");
                }
            }
        }
        return where.length()==0?null:where.toString();
    }

}
