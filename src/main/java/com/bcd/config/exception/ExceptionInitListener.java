package com.bcd.config.exception;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认HandlerExceptionResolverComposite加载源码在
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver}
 *
 * 在系统加载完毕之后,取出spring的默认异常处理器
 * 把默认的 DefaultHandlerExceptionResolver 替换成 自定义的异常解析器
 *
 * 为什么不直接通过Bean方式替换
 * 因为spring默认的是 HandlerExceptionResolverComposite,其中包含3个 HandlerExceptionResolver,按照优先级选择
 * 1、{@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver},
 *      负责处理 {@link org.springframework.web.bind.annotation.ExceptionHandler}
 * 2、{@link org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver},
 *      负责处理 {@link org.springframework.web.bind.annotation.ResponseStatus}
 * 3、{@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver},默认的异常处理器
 *
 * 如果替换掉会导致
 * {@link org.springframework.web.bind.annotation.ExceptionHandler}
 * {@link org.springframework.web.bind.annotation.ResponseStatus}
 * 失效
 * 我们的目的只是替换默认异常处理器,所以需要通过此方式
 *
 *
 */
@Component
public class ExceptionInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ExceptionResponseHandler exceptionResponseHandler=contextRefreshedEvent.getApplicationContext().getBean(ExceptionResponseHandler.class);
        HandlerExceptionResolverComposite handlerExceptionResolverComposite=(HandlerExceptionResolverComposite)contextRefreshedEvent.getApplicationContext().getBean("handlerExceptionResolverComposite");
        List<HandlerExceptionResolver> resolvers = new ArrayList<>(handlerExceptionResolverComposite.getExceptionResolvers().subList(0, handlerExceptionResolverComposite.getExceptionResolvers().size() - 1));
        resolvers.add(new CustomExceptionHandler(exceptionResponseHandler));
        handlerExceptionResolverComposite.setExceptionResolvers(Collections.unmodifiableList(resolvers));
    }
}
