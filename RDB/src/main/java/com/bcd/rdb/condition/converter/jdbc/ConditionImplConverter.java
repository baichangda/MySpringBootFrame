package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConditionImpl;
import com.bcd.rdb.util.ConditionUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class ConditionImplConverter implements Converter<ConditionImpl,String> {
    public static boolean IS_SQL_FORMAT=true;
    public final static String BLANK="  ";
    @Override
    public String convert(ConditionImpl condition, Object... exts) {
        List<Condition> childrenList=condition.childrenList;
        ConditionImpl.ConcatWay concatWay=condition.concatWay;
        Map<String,Object> paramMap=(Map<String,Object>)exts[0];
        Map<String,Integer> paramToCount=(Map<String,Integer>)exts[1];
        int deep=(int)exts[2];
        List<Object[]> childrenValList=childrenList.stream().map(e-> new Object[]{ConditionUtil.convertCondition(e,paramMap,paramToCount,deep+1),e instanceof ConditionImpl}).filter(e->e[0]!=null).collect(Collectors.toList());
        String concat;
        if(ConditionImpl.ConcatWay.AND.equals(concatWay)){
            concat="AND";
        }else if(ConditionImpl.ConcatWay.OR.equals(concatWay)){
            concat="OR";
        }else{
            return null;
        }
        if(IS_SQL_FORMAT){
            return childrenValList.stream().map(e->{
                if((boolean)e[1]){
                    StringBuilder sb=new StringBuilder();
                    sb.append("(");
                    sb.append("\n");
                    for(int i=1;i<=deep;i++){
                        sb.append(BLANK);
                    }
                    sb.append(e[0]);
                    sb.append("\n");
                    for(int i=1;i<deep;i++){
                        sb.append(BLANK);
                    }

                    sb.append(")");
                    return sb.toString();
                }else{
                    return "("+e[0]+")";
                }
            }).reduce((e1,e2)->{
                StringBuilder sb=new StringBuilder();
                sb.append(e1);
                sb.append("\n");
                for(int i=1;i<deep;i++){
                    sb.append(BLANK);
                }
                sb.append(concat);
                sb.append(" ");
                sb.append(e2);
                return sb.toString();
            }).orElse(null);
        }else{
            return childrenValList.stream().map(e->"("+e[0]+")").reduce((e1,e2)->{
                StringBuilder sb=new StringBuilder();
                sb.append(e1);
                sb.append(" ");
                sb.append(concat);
                sb.append(" ");
                sb.append(e2);
                return sb.toString();
            }).orElse(null);
        }
    }
}
