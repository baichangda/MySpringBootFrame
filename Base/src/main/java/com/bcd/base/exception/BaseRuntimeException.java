package com.bcd.base.exception;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;

/**
 * 建造此异常类的目的:
 * 1、在所有需要抛非运行时异常的地方,用此异常包装,避免方法调用时候需要捕获异常(若是其他框架自定义的异常,请不要用此类包装)
 * 2、在业务需要出异常的时候,定义异常并且抛出
 *
 * 注意:
 * 如果是用作第一种用途,则所有继承自Throwable的方法都是针对解析出来的真实异常,解析规则参考 ExceptionUtil.parseRealException
 */
public class BaseRuntimeException extends RuntimeException{
    protected String code;

    public String getCode() {
        return code;
    }

    public BaseRuntimeException(String message) {
        super(message);
    }
    public BaseRuntimeException(String message, String code) {
        this(message);
        this.code=code;
    }
    public BaseRuntimeException(Throwable e) {
        super(e);
    }
    public BaseRuntimeException(Throwable e, String code) {
        this(e);
        this.code=code;
    }

    public static BaseRuntimeException getException(String message){
        return new BaseRuntimeException(message);
    }
    public static BaseRuntimeException getException(String message,String code){
        return new BaseRuntimeException(message,code);
    }
    public static BaseRuntimeException getException(Throwable e){
        return new BaseRuntimeException(e);
    }
    public static BaseRuntimeException getException(Throwable e,String code){
        return new BaseRuntimeException(e,code);
    }

    public JsonMessage toJsonMessage(){
        return ExceptionUtil.toJsonMessage(this);
    }
}
