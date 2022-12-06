package com.bcd.base.condition.impl;

import com.bcd.base.condition.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/23.
 */
public class ConcatCondition implements Condition {
    public final ConcatWay concatWay;
    public final List<Condition> conditions;

    public ConcatCondition(ConcatWay concatWay, List<Condition> conditions) {
        this.concatWay = concatWay;
        this.conditions = conditions;
    }

    @Override
    public String toAnalysis() {
        List<String> list = new ArrayList<>();
        for (Condition condition : conditions) {
            String cur = condition.toAnalysis();
            if (cur != null) {
                list.add(cur);
            }
        }
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            return "(" +
                    list.stream().reduce((e1, e2) -> e1 + " " + concatWay.toString() + " " + e2).orElse("") +
                    ")";
        }
    }

    public enum ConcatWay {
        AND,
        OR
    }

}
