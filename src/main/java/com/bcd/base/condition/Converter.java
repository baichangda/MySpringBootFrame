package com.bcd.base.condition;


/**
 * Created by Administrator on 2017/9/15.
 */
public interface Converter<T extends Condition, R> {
    R convert(T condition, Object... exts);
}
