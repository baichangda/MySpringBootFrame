package com.bcd.config.exception;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.config.exception.handler.impl.DefaultExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;


/**
 * 定义异常响应处理器
 */
@Configuration
public class ExceptionConfig {
    @Bean
    @ConditionalOnMissingBean
    public ExceptionResponseHandler exceptionResponseHandler(@Qualifier("httpMessageConverter") HttpMessageConverter converter) {
        ExceptionResponseHandler handler=new DefaultExceptionResponseHandler(converter);
        return handler;
    }
}
