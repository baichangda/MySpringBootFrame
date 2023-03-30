package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

/**
 * 是否为null条件
 * 不依赖val
 */
public class NullCondition implements Condition {
    public final Handler handler;
    public final String fieldName;

    private NullCondition(String fieldName, Handler handler) {
        this.fieldName = fieldName;
        this.handler = handler;
    }

    @Override
    public String toAnalysis() {
        return fieldName +
                " " +
                handler.toString();
    }

    public static NullCondition NULL(String fieldName) {
        return new NullCondition(fieldName, Handler.NULL);
    }

    public static NullCondition NOT_NULL(String fieldName) {
        return new NullCondition(fieldName, Handler.NOT_NULL);
    }

    public enum Handler {
        /**
         * 为空
         */
        NULL,
        /**
         * 不为空
         */
        NOT_NULL
    }
}
