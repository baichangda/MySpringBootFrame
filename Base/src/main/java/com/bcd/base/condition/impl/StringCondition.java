package com.bcd.base.condition.impl;


import com.bcd.base.condition.Condition;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日期类型条件
 * 当val==null时候忽略此条件
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

    @Override
    public String toAnalysis() {
        return val==null||"".equals(val)?null:fieldName +
                " " +
                handler.toString();
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
