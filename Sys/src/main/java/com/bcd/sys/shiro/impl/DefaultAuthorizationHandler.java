package com.bcd.sys.shiro.impl;

import com.bcd.sys.shiro.AuthorizationHandler;
import org.apache.shiro.aop.MethodInvocation;

public class DefaultAuthorizationHandler implements AuthorizationHandler {
    @Override
    public boolean isValidate(MethodInvocation methodInvocation) {
//        UserBean userBean= ShiroUtil.getCurrentUser();
//        if(userBean==null||CommonConst.ADMIN_ID.equals(userBean.getId())){
//            return false;
//        }else{
//            return true;
//        }
        return true;
    }
}
