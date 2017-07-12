package com.base.util;


import com.base.condition.BaseCondition;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/26.
 */
@SuppressWarnings("unchecked")
public class ConditionUtil {


    /**
     * 根据baseCondition集合构造Specification查询条件
     *
     * @param conditionList
     * @param <T>
     * @return
     */
    public static <T> Specification<T> getSpecificationByCondition(final List<BaseCondition> conditionList) {
        Specification<T> specification =
                (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                    List<Predicate> paramList = new ArrayList<Predicate>();
                    for (int i = 0; i <= conditionList.size() - 1; i++) {
                        BaseCondition condition = conditionList.get(i);
                        Predicate predicate = condition.toPredicate(root, query, cb);
                        if (predicate != null) {
                            paramList.add(predicate);
                        }
                    }
                    return cb.and(paramList.toArray(new Predicate[paramList.size()]));
                };

        return specification;
    }


    /**
     * 根据baseCondition构造Specification查询条件
     * @param condition
     * @param <T>
     * @return
     */
    public static <T> Specification<T> getSpecificationByCondition(final BaseCondition condition) {
        Specification<T> specification=
                (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                    Predicate predicate= condition.toPredicate(root,query,cb);
                    if(predicate==null){
                        return cb.and();
                    }else{
                        return predicate;
                    }
                };
        return specification;
    }


}
