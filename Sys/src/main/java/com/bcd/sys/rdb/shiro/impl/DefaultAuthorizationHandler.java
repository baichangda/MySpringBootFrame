package com.bcd.sys.rdb.shiro.impl;

import com.bcd.sys.rdb.bean.UserBean;
import com.bcd.sys.rdb.define.CommonConst;
import com.bcd.base.config.shiro.AuthorizationHandler;
import com.bcd.sys.shiro.ShiroUtil;
import org.apache.shiro.aop.MethodInvocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Component
public class DefaultAuthorizationHandler implements AuthorizationHandler {
    @Override
    public boolean skip(MethodInvocation methodInvocation) {
        return skip();
    }

    @Override
    public boolean skip(ServletRequest request, ServletResponse response, Object mappedValue) {
        return skip();
    }

    /**
     * 配置管理员用户跳过权限认证
     * @return
     */
    private boolean skip(){
        UserBean userBean= ShiroUtil.getCurrentUser();
        if(userBean!=null&& CommonConst.ADMIN_ID.equals(userBean.getId())){
            return true;
        }else{
            return false;
        }
    }
}
