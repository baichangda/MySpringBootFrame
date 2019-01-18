package com.bcd.sys.mongodb.listener;

import com.bcd.base.condition.impl.StringCondition;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.mongodb.bean.UserBean;
import com.bcd.sys.mongodb.service.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements ApplicationListener<ContextRefreshedEvent>{
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initAdmin(event);
    }

    /**
     * 初始化管理员用户
     * @param event
     */
    private void initAdmin(ContextRefreshedEvent event){
        String username="admin";
        UserService userService= event.getApplicationContext().getBean(UserService.class);
        UserBean userBean= userService.findOne(new StringCondition("username",username));
        if(userBean==null){
            userBean=new UserBean();
            userBean.setUsername(username);
            String password;
            if(CommonConst.IS_PASSWORD_ENCODED){
                password=userService.encryptPassword(username, CommonConst.INITIAL_PASSWORD);
            }else{
                password= CommonConst.INITIAL_PASSWORD;
            }
            userBean.setPassword(password);
            userBean.setStatus(1);
            userService.save(userBean);
        }
    }
}
