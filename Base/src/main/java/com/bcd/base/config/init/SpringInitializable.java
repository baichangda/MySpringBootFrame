package com.bcd.base.config.init;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * spring初始化接口，实现了此接口的子类会被扫描出来并执行init方法
 * 注意:
 * 实现此接口的init方法里面逻辑不能有互相依赖的关系
 * 因为不能保证其初始化顺序
 */
public interface SpringInitializable {
    void init(ContextRefreshedEvent event);

    default void destroy(){};
}
