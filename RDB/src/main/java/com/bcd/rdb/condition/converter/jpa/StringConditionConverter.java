package com.bcd.rdb.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.util.ConditionUtil;
import org.assertj.core.util.Arrays;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class StringConditionConverter implements Converter<StringCondition,Predicate> {
    @Override
    public Predicate convert(StringCondition condition, Object... exts) {
        StringCondition.Handler handler= condition.handler;
        Object val=condition.val;
        String fieldName=condition.fieldName;
        Predicate predicate=null;
        Root root=(Root)exts[0];
        CriteriaQuery query=(CriteriaQuery)exts[1];
        CriteriaBuilder cb=(CriteriaBuilder)exts[2];
        if(!StringUtils.isEmpty(val)){
            Path path = ConditionUtil.parseRootPath(root,fieldName);
            switch (handler){
                case EQUAL: {
                    predicate=cb.equal(path, val);
                    break;
                }
                case NOT_EQUAL: {
                    predicate=cb.notEqual(path, val);
                    break;
                }
                case ALL_LIKE: {
                    predicate=cb.like(path, "%" + val.toString() + "%");
                    break;
                }
                case LEFT_LIKE: {
                    predicate=cb.like(path, "%" + val.toString());
                    break;
                }
                case RIGHT_LIKE: {
                    predicate=cb.like(path, val.toString() + "%");
                    break;
                }
                case IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(e->e!=null).collect(Collectors.toList());
                        predicate=path.in(notEmptyList);
                    }else if(Arrays.isArray(val)){
                        List notEmptyList=Arrays.asList(val).stream().filter(e->e!=null).collect(Collectors.toList());
                        predicate=path.in(notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(e->e!=null).collect(Collectors.toList());
                        predicate=cb.not(path.in(notEmptyList));
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
        return predicate;
    }

}
