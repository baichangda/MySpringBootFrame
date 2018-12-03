package com.bcd.sys.shiro.impl;

import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.shiro.CurrentUserValidateHandler;
import com.bcd.sys.util.ShiroUtil;
import org.apache.shiro.aop.MethodInvocation;

public class DefaultCurrentUserValidateHandler implements CurrentUserValidateHandler {
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
