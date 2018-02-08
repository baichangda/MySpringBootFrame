package com.bcd.config.shiro.anno;

import com.bcd.sys.bean.UserBean;
import com.bcd.sys.util.ShiroUtil;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class UserInfoAnnotationHandler extends AuthorizingAnnotationHandler {

    public UserInfoAnnotationHandler() {
        super(RequiresUserInfo.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void assertAuthorized(Annotation a) throws AuthorizationException {
        if (!(a instanceof RequiresUserInfo)) return;
        long id=((RequiresUserInfo)a).id();
        String username=((RequiresUserInfo)a).username();
        Logical logical=((RequiresUserInfo)a).logical();
        UserBean userBean= ShiroUtil.getCurrentUser();
        List<Boolean> conditionList=new ArrayList<>();
        if(id!=0){
            conditionList.add(userBean.getId()==id);
        }
        if(!"".equals(username)){
            conditionList.add(username.equals(userBean.getUsername()));
        }
        if(conditionList.size()==0){
            return;
        }
        if(logical==Logical.AND){
            if(conditionList.stream().anyMatch(e->!e)){
                throw new AuthorizationException("");
            }
        }else if(logical==Logical.OR){
            if(conditionList.stream().allMatch(e->!e)){
                throw new AuthorizationException("");
            }
        }

    }

}