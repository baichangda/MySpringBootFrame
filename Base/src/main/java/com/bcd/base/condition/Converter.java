package com.bcd.base.condition;

/**
 * Created by Administrator on 2017/9/15.
 */
public interface Converter<T,R> {
     R convert(T condition, Object... exts);
}
