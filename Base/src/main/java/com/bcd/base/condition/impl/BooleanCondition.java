package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

/**
 * Created by Administrator on 2017/10/11.
 */
public class BooleanCondition extends Condition {
    public BooleanCondition(String fieldName, Boolean val){
        this.fieldName=fieldName;
        this.val=val;
    }

    public BooleanCondition(String fieldName){
        this(fieldName,true);
    }
}
