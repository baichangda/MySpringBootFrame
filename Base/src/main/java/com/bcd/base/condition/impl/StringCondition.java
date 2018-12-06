package com.bcd.base.condition.impl;


import com.bcd.base.condition.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/12/30.
 */
@SuppressWarnings("unchecked")
public class StringCondition extends Condition {
    public Handler handler;

    public StringCondition(String fieldName, Object val, Handler handler){
        this.fieldName=fieldName;
        this.val=val;
        this.handler=handler;
    }

    public StringCondition(String fieldName, Object val){
        this(fieldName,val,Handler.EQUAL);
    }

    public static Condition or(String fieldName,Handler handler,Object ... vals){
        if(fieldName==null||vals==null||handler==null||vals.length==0){
            return null;
        }
        List<Condition> conditionList= Arrays.stream(vals).map(val->new StringCondition(fieldName,val,handler)).collect(Collectors.toList());
        return or(conditionList);
    }

    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 不等于
         */
        NOT_EQUAL,
        /**
         * 全匹配
         */
        ALL_LIKE,
        /**
         * 左匹配
         */
        LEFT_LIKE,
        /**
         * 右匹配
         */
        RIGHT_LIKE,
        /**
         * 在...之内
         */
        IN,
        /**
         * 不在...之内
         */
        NOT_IN
    }
}
