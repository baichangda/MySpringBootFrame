package com.bcd.rdb.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.util.ConditionUtil;

import javax.persistence.criteria.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        if(val!=null){
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
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        predicate=path.in(notEmptyList);
                    }else if(val.getClass().isArray()){
                        List notEmptyList=new ArrayList();
                        int len= Array.getLength(val);
                        if(len!=0){
                            for(int i=0;i<=len-1;i++){
                                Object o=Array.get(val,i);
                                if(o!=null){
                                    notEmptyList.add(o);
                                }
                            }
                        }
                        predicate=path.in(notEmptyList);
                    }else{
                        throw BaseRuntimeException.getException("[StringConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if(val instanceof Collection){
                        List notEmptyList= (List)((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
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
