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
    public List<Condition> childrenList;

    public ConditionImpl(ConcatWay concatWay, List<Condition> childrenList){
        this.concatWay=concatWay;
        this.childrenList=childrenList;
    }

    public enum ConcatWay{
        AND,
        OR
    }

    @Override
    public String toAnalysis() {
        List<String> list=new ArrayList<>();
        for (Condition condition : childrenList) {
            String cur=condition.toAnalysis();
            if(cur!=null){
                list.add(cur);
            }
        }
        if(list.isEmpty()){
            return null;
        }else if(list.size()==1){
            return list.get(0);
        }else{
            return "(" +
                    list.stream().reduce((e1, e2) -> e1 +" "+ concatWay.toString() + " "+e2).orElse("") +
                    ")";
        }
    }

}
