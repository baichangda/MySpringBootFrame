package com.bcd.config.init;

import com.bcd.base.config.init.anno.SpringInitializable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringInitListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String,SpringInitializable> beanMap= event.getApplicationContext().getBeansOfType(SpringInitializable.class);
        beanMap.values().forEach(e->e.init(event));
    }
}
