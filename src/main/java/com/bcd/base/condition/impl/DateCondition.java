package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

import java.util.Date;

/**
 * 日期类型条件
 * 当val==null时候忽略此条件
 */
public class DateCondition implements Condition {
    public final Handler handler;
    public final String fieldName;
    public final Object val;

    private DateCondition(String fieldName, Object val, Handler handler) {
        this.fieldName = fieldName;
        this.val = val;
        this.handler = handler;
    }

    @Override
    public String toAnalysis() {
        return val == null ? null : fieldName +
                " " +
                handler.toString();
    }

    public static DateCondition EQUAL(String fieldName, Date val) {
        return new DateCondition(fieldName, val, DateCondition.Handler.EQUAL);
    }

    public static DateCondition LE(String fieldName, Date val) {
        return new DateCondition(fieldName, val, DateCondition.Handler.EQUAL);
    }

    public static DateCondition LT(String fieldName, Date val) {
        return new DateCondition(fieldName, val, DateCondition.Handler.EQUAL);
    }

    public static DateCondition GE(String fieldName, Date val) {
        return new DateCondition(fieldName, val, DateCondition.Handler.EQUAL);
    }

    public static DateCondition GT(String fieldName, Date val) {
        return new DateCondition(fieldName, val, DateCondition.Handler.EQUAL);
    }

    public static DateCondition BETWEEN(String fieldName, Date start, Date end) {
        return new DateCondition(fieldName, new Date[]{start, end}, DateCondition.Handler.BETWEEN);
    }

    public enum Handler {
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
        GT,
        /**
         * 在时间范围内、前闭后开[date1,date2)
         */
        BETWEEN
    }
}
