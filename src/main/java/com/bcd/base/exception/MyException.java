package com.bcd.base.exception;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.StringUtil;

/**
 * 建造此异常类的目的:
 * 1、在所有需要抛非运行时异常的地方,用此异常包装,避免方法调用时候需要捕获异常(若是其他框架自定义的异常,请不要用此类包装)
 * 2、在业务需要出异常的时候,定义异常并且抛出
 * <p>
 * 注意:
 * 如果是用作第一种用途,则所有继承自Throwable的方法都是针对解析出来的真实异常,解析规则参考 {@link ExceptionUtil#parseR
 * ealException}
 */
public class MyException extends RuntimeException {
    public int code = 1;

    private MyException(String message) {
        super(message);
    }

    private MyException(Throwable e) {
        super(e);
    }

    public static MyException get(String message) {
        return new MyException(message);
    }

    /**
     * 将异常信息转换为格式化
     * 使用方式和sl4j log一样、例如
     * {@link org.slf4j.Logger#info(String, Object...)}
     *
     * @param message
     * @param params
     * @return
     */
    public static MyException get(String message, Object... params) {
        return new MyException(StringUtil.format(message, params));
    }

    public static MyException get(Throwable e) {
        return new MyException(e);
    }

    public static void main(String[] args) {
        throw MyException.get("[{}]-[{}]", null, 100000);
    }

    public MyException code(int code) {
        this.code = code;
        return this;
    }
}
