package com.bcd.base.support_spring_exception;

import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import com.bcd.base.support_spring_exception.handler.impl.DefaultExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;


/**
 * 定义异常响应处理器
 */
@Configuration
public class ExceptionConfig {
    @Bean
    public ExceptionResponseHandler exceptionResponseHandler(@Qualifier("mappingJackson2HttpMessageConverter_my") MappingJackson2HttpMessageConverter converter) {
        ExceptionResponseHandler handler = new DefaultExceptionResponseHandler(converter);
        return handler;
    }

    /**
     * 生成此bean原因是因为在spring cloud模式下、需要一个{@link HandlerExceptionResolverComposite}类型异常解析器
     *
     * @param handlerExceptionResolver
     * @return
     */
    @Bean("handlerExceptionResolverComposite")
    public HandlerExceptionResolverComposite handlerExceptionResolverComposite(HandlerExceptionResolver handlerExceptionResolver) {
        return (HandlerExceptionResolverComposite) handlerExceptionResolver;
    }

}
