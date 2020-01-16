package com.bcd.config.init;

import com.bcd.base.config.init.SpringInitializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringInitListener implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger= LoggerFactory.getLogger(SpringInitListener.class);
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String,SpringInitializable> beanMap= event.getApplicationContext().getBeansOfType(SpringInitializable.class);
            beanMap.values().forEach(e -> {
                try{
                    e.init(event);
                }catch (Exception ex){
                    logger.error("SpringInitListener["+e.getClass()+"] init failed,shutdown...",ex);
                    e.destroy();
                }
            });

    }
}
