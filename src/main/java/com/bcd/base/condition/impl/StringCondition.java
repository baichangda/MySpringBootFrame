package com.bcd.base.condition.impl;


import com.bcd.base.condition.Condition;

/**
 * 日期类型条件
 * 当val==null时候忽略此条件
 */
public class StringCondition implements Condition {
    public final Handler handler;
    public final String fieldName;
    public final Object val;

    private StringCondition(String fieldName, Object val, Handler handler) {
        this.fieldName = fieldName;
        this.val = val;
        this.handler = handler;
    }

    public static StringCondition EQUAL(String fieldName, String val) {
        return new StringCondition(fieldName, val, Handler.EQUAL);
    }

    public static StringCondition NOT_EQUAL(String fieldName, String val) {
        return new StringCondition(fieldName, val, Handler.NOT_EQUAL);
    }

    public static StringCondition ALL_LIKE(String fieldName, String val) {
        return new StringCondition(fieldName, val, Handler.ALL_LIKE);
    }

    public static StringCondition LEFT_LIKE(String fieldName, String val) {
        return new StringCondition(fieldName, val, Handler.LEFT_LIKE);
    }

    public static StringCondition RIGHT_LIKE(String fieldName, String val) {
        return new StringCondition(fieldName, val, Handler.RIGHT_LIKE);
    }

    public static StringCondition IN(String fieldName, String... val) {
        return new StringCondition(fieldName, val, Handler.IN);
    }

    public static StringCondition NOT_IN(String fieldName, String... val) {
        return new StringCondition(fieldName, val, Handler.NOT_IN);
    }

    public enum Handler {
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
