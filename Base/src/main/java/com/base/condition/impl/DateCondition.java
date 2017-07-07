package com.base.condition.impl;

import com.base.condition.BaseCondition;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/12/30.
 */
@SuppressWarnings("unchecked")
public class DateCondition extends BaseCondition {
    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 小于等于
         */
        LE,
        /**
         * 小于
         */
        LT,
        /**
         * 大于等于
         */
        GE,
        /**
         * 大于
         */
        GT,
        /**
         * 在当前日期天内
         */
        IN_YEAR_MONTH_DAY,
        /**
         * 在当前日期月内
         */
        IN_YEAR_MONTH
    }

    private Handler handler;

    public DateCondition(String fieldName,Object val,Handler handler){
        super(fieldName,val);
        this.handler=handler;
    }

    public DateCondition(String fieldName,Object val){
        super(fieldName,val);
        if(val instanceof Date){
            this.handler= Handler.EQUAL;
        }
    }

    public <T> Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate=null;
        if(val!=null) {
            Path path = parseRootPath(root, fieldName);
            switch (handler) {
                case EQUAL: {
                    predicate = cb.equal(path, val);
                    break;
                }
                case LE: {
                    predicate = cb.lessThanOrEqualTo(path, (Date) val);
                    break;
                }
                case LT: {
                    predicate = cb.lessThan(path, (Date) val);
                    break;
                }
                case GE: {
                    predicate = cb.greaterThanOrEqualTo(path, (Date) val);
                    break;
                }
                case GT: {
                    predicate = cb.greaterThan(path, (Date) val);
                    break;
                }
                case IN_YEAR_MONTH_DAY: {
                    List<Predicate> predicateList = new ArrayList<>();
                    Calendar beginCalendar = Calendar.getInstance();
                    Calendar endCalendar = Calendar.getInstance();
                    beginCalendar.setTime((Date) val);
                    endCalendar.setTime((Date) val);

                    beginCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    beginCalendar.set(Calendar.MINUTE, 0);
                    beginCalendar.set(Calendar.SECOND, 0);
                    beginCalendar.set(Calendar.MILLISECOND, 0);

                    endCalendar.add(Calendar.DATE, 1);
                    endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    endCalendar.set(Calendar.MINUTE, 0);
                    endCalendar.set(Calendar.SECOND, 0);
                    endCalendar.set(Calendar.MILLISECOND, 0);

                    predicateList.add(cb.greaterThanOrEqualTo(path, beginCalendar.getTime()));
                    predicateList.add(cb.lessThan(path, endCalendar.getTime()));
                    predicate = cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
                    break;
                }
                case IN_YEAR_MONTH: {
                    List<Predicate> predicateList = new ArrayList<>();
                    Calendar beginCalendar = Calendar.getInstance();
                    Calendar endCalendar = Calendar.getInstance();
                    beginCalendar.setTime((Date) val);
                    endCalendar.setTime((Date) val);

                    beginCalendar.set(Calendar.DATE, 0);
                    beginCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    beginCalendar.set(Calendar.MINUTE, 0);
                    beginCalendar.set(Calendar.SECOND, 0);
                    beginCalendar.set(Calendar.MILLISECOND, 0);

                    endCalendar.add(Calendar.MONTH, 1);
                    endCalendar.set(Calendar.DATE, 0);
                    endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    endCalendar.set(Calendar.MINUTE, 0);
                    endCalendar.set(Calendar.SECOND, 0);
                    endCalendar.set(Calendar.MILLISECOND, 0);

                    predicateList.add(cb.greaterThanOrEqualTo(path, beginCalendar.getTime()));
                    predicateList.add(cb.lessThan(path, endCalendar.getTime()));
                    predicate = cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
                    break;
                }
            }
        }
        return predicate;
    }

}
