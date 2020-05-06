package com.bcd.config.exception;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.config.exception.handler.impl.DefaultExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


/**
 * 定义异常响应处理器
 */
@Configuration
public class ExceptionConfig {
    @Bean
    public ExceptionResponseHandler exceptionResponseHandler(@Qualifier("mappingJackson2HttpMessageConverter") MappingJackson2HttpMessageConverter converter) {
        ExceptionResponseHandler handler=new DefaultExceptionResponseHandler(converter);
        return handler;
    }
}
