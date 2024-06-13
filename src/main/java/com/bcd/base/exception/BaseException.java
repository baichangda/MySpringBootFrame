package com.bcd.base.exception;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.StringUtil;

/**
 * 建造此异常类的目的:
 * 1、在所有需要抛非运行时异常的地方,用此异常包装,避免方法调用时候需要捕获异常(若是其他框架自定义的异常,请不要用此类包装)
 * 2、在业务需要出异常的时候,定义异常并且抛出
 * <p>
 * 注意:
 * 如果是用作第一种用途,则所有继承自Throwable的方法都是针对解析出来的真实异常,解析规则参考 {@link ExceptionUtil#parseException}
 */
public class BaseException extends RuntimeException {
    public int code = 1;

    private BaseException(String message) {
        super(message);
    }

    private BaseException(Throwable e) {
        super(e);
    }

    public static BaseException get(String message) {
        return new BaseException(message);
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
    public static BaseException get(String message, Object... params) {
        return new BaseException(StringUtil.format(message, params));
    }

    public static BaseException get(Throwable e) {
        return new BaseException(e);
    }

    public static void main(String[] args) {
        throw BaseException.get("[{}]-[{}]", null, 100000);
    }

    public BaseException code(int code) {
        this.code = code;
        return this;
    }
}
