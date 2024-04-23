package com.bcd.base.util;

import com.bcd.base.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Created by Administrator on 2017/7/27.
 */
public class ExceptionUtil {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionUtil.class);

    /**
     * 获取堆栈信息
     *
     * @param throwable
     * @return
     */
    public static String getStackTraceMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            throw MyException.get(e);
        }
    }


    /**
     * 根据异常打印异常信息
     *
     * @param throwable
     * @return
     */
    public static void printException(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable realException = parseRealException(throwable);
        logger.error("Error", realException);
    }

    /**
     * 获取异常的信息
     * @param throwable
     * @return
     */
    public static String getMessage(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable realException = parseRealException(throwable);
        return realException.getMessage();
    }


    /**
     * 遇到如下两种情况继续深入取出异常信息:
     * 1、getCause()==null
     * 2、InvocationTargetException
     *
     * @param throwable
     * @return
     */
    public static Throwable parseRealException(Throwable throwable) {
        //1、如果异常为空,返回null
        if (throwable == null) {
            return null;
        }
        //2、获取其真实异常
        Throwable realException = throwable.getCause();
        //3、如果真实异常为当前异常
        if (realException == null) {
            //4、如果真实异常为InvocationTargetException,则获取其目标异常
            if (throwable instanceof InvocationTargetException) {
                return parseRealException(((InvocationTargetException) throwable).getTargetException());
            } else {
                //5、否则直接返回
                return throwable;
            }
        } else {
            //6、如果真实异常不为当前异常,则继续解析其真实异常
            return parseRealException(realException);
        }
    }
}
