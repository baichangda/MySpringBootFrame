package com.bcd.config.exception;

import com.bcd.config.exception.handler.ExceptionResponseHandler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在系统加载完毕之后,取出spring的默认异常处理器
 * 把默认的 DefaultHandlerExceptionResolver 替换成 自定义的异常解析器
 */
@Component
public class ExceptionInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ExceptionResponseHandler exceptionResponseHandler=contextRefreshedEvent.getApplicationContext().getBean(ExceptionResponseHandler.class);
        HandlerExceptionResolverComposite handlerExceptionResolverComposite=contextRefreshedEvent.getApplicationContext().getBean(HandlerExceptionResolverComposite.class);
        List<HandlerExceptionResolver> resolvers=new ArrayList<>();
        resolvers.addAll(handlerExceptionResolverComposite.getExceptionResolvers().subList(0,handlerExceptionResolverComposite.getExceptionResolvers().size()-1));
        resolvers.add(new CustomExceptionHandler(exceptionResponseHandler));
        handlerExceptionResolverComposite.setExceptionResolvers(Collections.unmodifiableList(resolvers));
    }
}
