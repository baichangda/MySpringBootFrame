package com.bcd.rdb.bean.info;

import com.bcd.rdb.service.BaseService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.FileSystems;
import java.util.Map;

/**
 * 必须要使用此类通过找到所有BaseService的子类,从而找到所有BaseService使用的BaseBean的子类
 * 并初始化这些BaseBean子类的实体信息;同时为BaseService赋值BeanInfo
 * remarks:
 * 不使用@PostConstruct注解的原因是因为@PostConstruct可能会比其他工具类的Bean初始化先执行(例如RedisOp,SpringUtil)
 * 必须等待所有Spring Bean初始化完成之后才初始化实体类信息
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class BeanInfoApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String,BaseService> serviceMap= event.getApplicationContext().getBeansOfType(BaseService.class);
        serviceMap.values().forEach(e->e._init_());
    }
}
