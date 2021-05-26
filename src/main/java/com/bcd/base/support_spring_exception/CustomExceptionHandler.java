package com.bcd.base.support_spring_exception;

import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义异常处理器,在ExceptionInitListener中替换掉spring默认的异常处理器
 */
@SuppressWarnings("unchecked")
public class CustomExceptionHandler extends AbstractHandlerExceptionResolver {
    private final static Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);
    private ExceptionResponseHandler handler;

    public CustomExceptionHandler(ExceptionResponseHandler handler) {
        this.handler = handler;
    }

    @Override
    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        return true;
    }

    @Override
    public ModelAndView doResolveException(HttpServletRequest request,
                                           HttpServletResponse response, Object object, Exception exception) {
        //1、判断response
        if (response.isCommitted()) {
            return new ModelAndView();
        }
        //2、打印异常
        logger.error("Error", exception);

        //3、使用异常handler处理异常
        try {
            handler.handle(response, exception);
        } catch (IOException e) {
            logger.error("Error", e);
        }
        return new ModelAndView();
    }
}