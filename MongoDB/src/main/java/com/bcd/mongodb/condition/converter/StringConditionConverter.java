package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.assertj.core.util.Arrays;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class StringConditionConverter implements Converter<StringCondition,Criteria> {
    @Override
    public Criteria convert(StringCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        Object val=condition.val;
        StringCondition.Handler handler=condition.handler;
        Criteria criteria= null;
        if(!StringUtils.isEmpty(val)){
            criteria=Criteria.where(fieldName);
            switch (handler){
                case EQUAL: {
                    criteria.is(val);
                    break;
                }
                case NOT_EQUAL: {
                    criteria.ne(val);
                    break;
                }
                case ALL_LIKE: {
                    criteria.regex(".*"+val.toString()+".*");
                    break;
                }
                case LEFT_LIKE: {
                    criteria.regex("^("+val.toString()+").*");
                    break;
                }
                case RIGHT_LIKE: {
                    criteria.regex(".*("+val.toString()+")$");
                    break;
                }
                case IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        criteria.in(notEmptyList);
                    }else if(Arrays.isArray(val)){
                        List notEmptyList=Arrays.asList(val).stream().filter(e->e!=null).collect(Collectors.toList());
                        criteria.in(notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        criteria.nin(notEmptyList);
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
        return criteria;
    }

}
