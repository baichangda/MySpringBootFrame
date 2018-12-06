package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/12/30.
 */
@SuppressWarnings("unchecked")
public class DateCondition extends Condition {
    public Handler handler;

    public DateCondition(String fieldName, Object val, Handler handler){
        this.fieldName=fieldName;
        this.val=val;
        this.handler=handler;
    }

    public static Condition or(String fieldName,Handler handler,Object ... vals){
        if(fieldName==null||vals==null||handler==null||vals.length==0){
            return null;
        }
        List<Condition> conditionList= Arrays.stream(vals).map(val->new DateCondition(fieldName,val,handler)).collect(Collectors.toList());
        return or(conditionList);
    }

    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 小于等于
         */
        LE,
        /**
         * 小于
         */
        LT,
        /**
         * 大于等于
         */
        GE,
        /**
         * 大于
         */
        GT
    }
}
