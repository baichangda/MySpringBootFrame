package com.bcd.sys.shiro.impl;

import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.shiro.AuthorizationHandler;
import com.bcd.sys.util.ShiroUtil;
import org.apache.shiro.aop.MethodInvocation;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
