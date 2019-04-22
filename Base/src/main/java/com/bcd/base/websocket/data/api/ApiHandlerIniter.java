package com.bcd.base.websocket.data.api;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.util.ProxyUtil;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Component
public class ApiHandlerIniter implements SpringInitializable {
    @Override
    public void init(ContextRefreshedEvent event) {
        ApiHandler.NAME_TO_HANDLER_MAP=event.getApplicationContext().getBeansOfType(WebSocketService.class).values().stream().flatMap(e->{
            Class clazz= ProxyUtil.getSource(e).getClass();
            return MethodUtils.getMethodsListWithAnnotation(clazz, WebSocketApi.class).stream().map(w->new Object[]{w,e});
        }).collect(Collectors.toMap(
                e->((Method)e[0]).getAnnotation(WebSocketApi.class).value(),
                e->new ApiHandler(((Method)e[0]),e[1]),
                (e1,e2)->e1
        ));
    }
}
