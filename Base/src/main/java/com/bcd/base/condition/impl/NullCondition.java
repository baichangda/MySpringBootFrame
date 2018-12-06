package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

/**
 * Created by Administrator on 2017/6/8.
 */
public class NullCondition extends Condition {
    public Handler handler;

    public NullCondition(String fieldName, Handler handler){
        this.fieldName=fieldName;
        this.handler=handler;
    }

    public NullCondition(String fieldName){
        this(fieldName,Handler.NULL);

    }

    public enum Handler{
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
