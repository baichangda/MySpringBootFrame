package com.bcd.sys.listener;

import com.bcd.sys.UserDataInit;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements ApplicationListener<ContextRefreshedEvent>{
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        init(event);
    }

    /**
     * 初始化管理员用户
     * @param event
     */
    private void init(ContextRefreshedEvent event){
        UserDataInit userDataInit= event.getApplicationContext().getBean(UserDataInit.class);
        userDataInit.init();
    }
}
