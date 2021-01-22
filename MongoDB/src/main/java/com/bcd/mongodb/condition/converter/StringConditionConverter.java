package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        if(val!=null){
            if(val instanceof Collection){
                criteria=Criteria.where(fieldName);
                List notEmptyList= (List)((Collection) val).stream().filter(e->e!=null&&!e.toString().isEmpty()).collect(Collectors.toList());
                switch (handler){
                    case IN:{
                        criteria.in(notEmptyList);
                        break;
                    }
                    case NOT_IN:{
                        criteria.nin(notEmptyList);
                        break;
                    }
                    default:{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                }
            }else if(val.getClass().isArray()){
                criteria=Criteria.where(fieldName);
                List notEmptyList=new ArrayList();
                int len= Array.getLength(val);
                if(len!=0){
                    for(int i=0;i<=len-1;i++){
                        Object o=Array.get(val,i);
                        if(o!=null&&!o.toString().isEmpty()){
                            notEmptyList.add(o);
                        }
                    }
                }
                switch (handler){
                    case IN:{
                        criteria.in(notEmptyList);
                        break;
                    }
                    case NOT_IN:{
                        criteria.nin(notEmptyList);
                        break;
                    }
                    default:{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Array Instance!");
                    }
                }
            }else{
                if(!val.toString().isEmpty()) {
                    criteria=Criteria.where(fieldName);
                    switch (handler) {
                        case EQUAL: {
                            criteria.is(val);
                            break;
                        }
                        case NOT_EQUAL: {
                            criteria.ne(val);
                            break;
                        }
                        case ALL_LIKE: {
                            criteria.regex(".*(" + val.toString() + ").*");
                            break;
                        }
                        case LEFT_LIKE: {
                            criteria.regex("^(" + val.toString() + ")");
                            break;
                        }
                        case RIGHT_LIKE: {
                            criteria.regex("(" + val.toString() + ")$");
                            break;
                        }
                        default: {
                            throw BaseRuntimeException.getException("[StringConditionConverter.convert],Do Not Support [" + handler + "]!");
                        }
                    }
                }
            }

        }
        return criteria;
    }

}
