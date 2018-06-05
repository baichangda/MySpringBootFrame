package com.bcd.config.exception;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.config.exception.handler.impl.DefaultExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@SuppressWarnings("unchecked")
public class CustomExceptionHandler extends DefaultHandlerExceptionResolver {
    private ExceptionResponseHandler handler;
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object object, Exception exception) {
        //1、判断response
        if(response.isCommitted()){
            return new ModelAndView();
        }
        //2、打印异常
        ExceptionUtil.printException(exception);

        //3、使用异常handler处理异常
        try {
            handler.handle(response,exception);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView();
    }

    @Bean
    @ConditionalOnMissingBean()
    public ExceptionResponseHandler exceptionResponseHandler(@Qualifier("fastJsonHttpMessageConverter4") HttpMessageConverter converter) {
        ExceptionResponseHandler handler=new DefaultExceptionResponseHandler(converter);
        this.handler=handler;
        return handler;
    }
}