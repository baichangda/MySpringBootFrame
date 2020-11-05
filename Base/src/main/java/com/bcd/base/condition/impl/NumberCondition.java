package com.bcd.base.condition.impl;


import com.bcd.base.condition.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日期类型条件
 * 当val==null时候忽略此条件
 */
@SuppressWarnings("unchecked")
public class NumberCondition extends Condition {
    public Handler handler;

    public NumberCondition(String fieldName, Object val, Handler handler){
        this.fieldName=fieldName;
        this.val=val;
        this.handler=handler;
    }

    public NumberCondition(String fieldName, Object val){
        this(fieldName,val,Handler.EQUAL);
    }

    public static Condition or(String fieldName, Handler handler, Object ... vals){
        if(fieldName==null||vals==null||handler==null||vals.length==0){
            return null;
        }
        List<Condition> conditionList= Arrays.stream(vals).map(val->new NumberCondition(fieldName,val,handler)).collect(Collectors.toList());
        return or(conditionList);
    }

    @Override
    public String toAnalysis() {
        return val==null?null:fieldName +
                " " +
                handler.toString();
    }

    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 小于
         */
        LT,
        /**
         * 小于等于
         */
        LE,
        /**
         * 大于
         */
        GT,
        /**
         * 大于等于
         */
        GE,
        /**
         * 不等于
         */
        NOT_EQUAL,
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
