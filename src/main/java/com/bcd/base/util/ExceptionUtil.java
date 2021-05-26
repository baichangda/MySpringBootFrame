package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Administrator on 2017/7/27.
 */
@SuppressWarnings("unchecked")
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
            throw BaseRuntimeException.getException(e);
        }
    }


    /**
     * 根据异常打印异常信息
     *
     * @param throwable
     * @return
     */
    public static void printException(Throwable throwable) {
        Throwable realException = parseRealException(throwable);
        logger.error("Error", realException);
    }

    /**
     * 根据异常生成JsonMessage
     *
     * @param throwable
     * @return
     */
    public static JsonMessage toJsonMessage(Throwable throwable) {
        Throwable realException = parseRealException(throwable);
        if (realException == null) {
            throw BaseRuntimeException.getException("ExceptionUtil.toJsonMessage Param[throwable] Can't Be Null");
        }
        if (realException instanceof BaseRuntimeException) {
            return JsonMessage.fail()
                    .code(((BaseRuntimeException) realException).getCode())
                    .message(realException.getMessage());
        }else {
            return JsonMessage.fail().message(realException.getMessage());
        }
    }

    public static String getMessage(Throwable throwable) {
        Throwable realException = parseRealException(throwable);
        if (realException == null) {
            throw BaseRuntimeException.getException("ExceptionUtil.toJsonMessage Param[throwable] Can't Be Null");
        }
        if (realException instanceof BaseRuntimeException) {
            return realException.getMessage();
        } else {
            return realException.getMessage();
        }
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
