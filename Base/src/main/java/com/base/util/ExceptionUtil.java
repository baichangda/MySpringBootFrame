package com.base.util;

import com.base.exception.BaseRuntimeException;

/**
 * Created by Administrator on 2017/7/27.
 */
public class ExceptionUtil {
    /**
     * 捕获非自定义base异常
     * 抛出自定义异常
     * @param catchException
     * @param throwException
     */
    public static BaseRuntimeException catchNonBaseRuntimeException(Exception catchException, BaseRuntimeException throwException){
        if(BaseRuntimeException.class.isAssignableFrom(catchException.getClass())){
            throw (BaseRuntimeException)catchException;
        }else{
            throw throwException;
        }
    }
}
