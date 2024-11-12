package com.bcd.base.exception;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * 建造此异常类的目的:
 * 1、在所有需要抛非运行时异常的地方,用此异常包装,避免方法调用时候需要捕获异常
 *    此时打印堆栈会打印完整的调用链、包括包装异常的调用链
 *    此时异常信息为null、如果需要获取真实异常信息、调用{@link #getRealExceptionMessage()}
 * 2、在业务需要出异常的时候,定义异常并且抛出
 */
public class BaseException extends RuntimeException {
    public int code = 1;

    private final Throwable target;

    private BaseException(String message) {
        super(message);
        this.target = null;
    }

    private BaseException(Throwable target) {
        super(null, (Throwable) null);
        this.target = target;
    }

    private BaseException(String message, Throwable target) {
        super(message, (Throwable) null);
        this.target = target;
    }

    public Throwable getTargetException() {
        return target;
    }

    @Override
    public Throwable getCause() {
        return target;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public String getRealExceptionMessage(){
        return ExceptionUtil.getRealException(this).getMessage();
    }


    public BaseException code(int code) {
        this.code = code;
        return this;
    }

    public static BaseException get(String message) {
        return new BaseException(message);
    }

    /**
     * 将异常信息转换为格式化
     * 使用方式和sl4j log一样、例如
     * {@link org.slf4j.Logger#info(String, Object...)}
     * 如果需要转义、则\\{}
     *
     * @param message
     * @param params
     * @return
     */
    public static BaseException get(String message, Object... params) {
        return new BaseException(StringUtil.format(message, params));
    }

    public static BaseException get(String message, Object arg) {
        return new BaseException(StringUtil.format(message, arg));
    }

    public static BaseException get(String message, Object arg1, Object arg2) {
        return new BaseException(StringUtil.format(message, arg1, arg2));
    }

    public static BaseException get(String message, Throwable e) {
        return new BaseException(message, e);
    }

    public static BaseException get(Throwable e) {
        return new BaseException(e);
    }

    static Logger logger = LoggerFactory.getLogger(BaseException.class);

    public static void main(String[] args) {
//        throw BaseException.get("[{}]-[{}]", null, 100000);

        try {
            String s = null;
            s.getBytes();
        } catch (Exception e) {
            BaseException e1 = BaseException.get(e);
            BaseException e2 = BaseException.get(e1);
            logger.error("error", e2);
            logger.info(e2.getMessage());
        }


        BaseException e = BaseException.get("测试");
        BaseException e1 = BaseException.get(e);
        BaseException e2 = BaseException.get(e1);
        logger.error("error", e2);
        logger.info(e2.getMessage());

        InvocationTargetException e3 = new InvocationTargetException(e);
        BaseException e4 = BaseException.get(e3);
        logger.error("error", e4);
        logger.info(e4.getMessage());

        BaseException e5 = BaseException.get(e);
        InvocationTargetException e6 = new InvocationTargetException(e5);
        logger.error("error", e6);
        logger.info(e6.getMessage());
    }

}
