package com.bcd.base.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;



/**
 * Created by Administrator on 2017/5/25.
 */
@Component
public class SpringUtil implements ApplicationContextAware{

    public static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        SpringUtil.applicationContext=applicationContext;
    }

}
