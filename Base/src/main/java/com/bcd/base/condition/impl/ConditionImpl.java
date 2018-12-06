package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/23.
 */
@SuppressWarnings("unchecked")
public class ConditionImpl extends Condition {
    public ConcatWay concatWay;
    public List<Condition> childrenList=new ArrayList<>();

    public ConditionImpl(ConcatWay concatWay, List<Condition> childrenList){
        this.concatWay=concatWay;
        this.childrenList=childrenList;
    }

    public enum ConcatWay{
        AND,
        OR
    }

}
