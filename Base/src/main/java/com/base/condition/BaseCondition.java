package com.base.condition;

import com.base.condition.impl.BaseConditionImpl;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public abstract class BaseCondition {
    public String fieldName;
    public Object val;

    public abstract <T>Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);


    public BaseCondition(String fieldName, Object val){
        this.fieldName=fieldName;
        this.val=val;
    }

    public BaseCondition(String fieldName){
        this.fieldName=fieldName;
    }
    public BaseCondition(){

    }

    public static BaseCondition and(List<BaseCondition> conditionList){
        return new BaseConditionImpl(BaseConditionImpl.ConcatWay.AND,conditionList);
    }

    public static BaseCondition and(BaseCondition ... conditionArr){
        return new BaseConditionImpl(BaseConditionImpl.ConcatWay.AND, Arrays.asList(conditionArr));
    }

    public static BaseCondition or(List<BaseCondition> conditionList){
        return new BaseConditionImpl(BaseConditionImpl.ConcatWay.OR, conditionList);
    }

    public static BaseCondition or(BaseCondition ... conditionArr){
        return new BaseConditionImpl(BaseConditionImpl.ConcatWay.OR, Arrays.asList(conditionArr));
    }


    public <T>Path parseRootPath(Root<T> root, String attrName){
        Path path=null;
        if(attrName.indexOf(".")!=-1){
            String [] attrArr=attrName.split("\\.");
            for(int i=0;i<=attrArr.length-1;i++){
                if(path==null){
                    path=root.get(attrArr[i]);
                }else{
                    path=path.get(attrArr[i]);
                }
            }
        }else{
            path=root.get(attrName);
        }
        return path;
    }
}
