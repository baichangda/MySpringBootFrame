package com.bcd.sys.shiro;

import com.bcd.base.support_shiro.AuthorizationHandler;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import org.apache.shiro.aop.MethodInvocation;
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
     *
     * @return
     */
    private boolean skip() {
        UserBean user = ShiroUtil.getCurrentUser();
        return user != null && CommonConst.ADMIN_ID == user.getId();
    }
}
