package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter  implements Converter<NumberCondition,Criteria> {
    @Override
    public Criteria convert(NumberCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        Object val=condition.val;
        NumberCondition.Handler handler=condition.handler;
        Criteria criteria= null;
        if(val!=null) {
            criteria=Criteria.where(fieldName);
            switch (handler) {
                case EQUAL: {
                    criteria.is(val);
                    break;
                }
                case LT: {
                    criteria.lt(val);
                    break;
                }
                case LE: {
                    criteria.lte(val);
                    break;
                }
                case GT: {
                    criteria.gt(val);
                    break;
                }
                case GE: {
                    criteria.gte(val);
                    break;
                }
                case NOT_EQUAL: {
                    criteria.ne(val);
                    break;
                }
                case IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        criteria.in(notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        criteria.nin(notEmptyList);
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
        return criteria;
    }
}
