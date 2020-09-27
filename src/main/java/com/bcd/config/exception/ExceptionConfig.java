package com.bcd.config.exception;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.config.exception.handler.impl.DefaultExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ConsumerPostProcessor;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;


/**
 * 定义异常响应处理器
 */
@Configuration
public class ExceptionConfig {
    @Bean
    public ExceptionResponseHandler exceptionResponseHandler(@Qualifier("mappingJackson2HttpMessageConverter_my") MappingJackson2HttpMessageConverter converter) {
        ExceptionResponseHandler handler=new DefaultExceptionResponseHandler(converter);
        return handler;
    }

    @Bean("handlerExceptionResolverComposite")
    public HandlerExceptionResolverComposite handlerExceptionResolverComposite(HandlerExceptionResolver handlerExceptionResolver){
        return (HandlerExceptionResolverComposite)handlerExceptionResolver;
    }

}
