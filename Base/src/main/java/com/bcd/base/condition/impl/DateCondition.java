package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

/**
 * 日期类型条件
 * 当val==null时候忽略此条件
 *
 */
@SuppressWarnings("unchecked")
public class DateCondition extends Condition {
    public Handler handler;

    public DateCondition(String fieldName, Object val, Handler handler){
        this.fieldName=fieldName;
        this.val=val;
        this.handler=handler;
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
