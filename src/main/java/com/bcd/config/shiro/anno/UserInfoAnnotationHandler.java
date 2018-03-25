package com.bcd.config.shiro.anno;

import com.bcd.sys.bean.UserBean;
import com.bcd.sys.util.ShiroUtil;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserInfoAnnotationHandler extends AuthorizingAnnotationHandler {

    public UserInfoAnnotationHandler() {
        super(RequiresUserInfo.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void assertAuthorized(Annotation a) throws AuthorizationException {
        if (!(a instanceof RequiresUserInfo)) return;
        long[] ids=((RequiresUserInfo)a).id();
        String[] usernames=((RequiresUserInfo)a).username();
        Logical logical=((RequiresUserInfo)a).logical();
        UserBean userBean= ShiroUtil.getCurrentUser();
        Stream<Boolean> idStream= Arrays.stream(ids).mapToObj(e->e==userBean.getId());
        Stream<Boolean> userNameStream= Arrays.stream(usernames).map(e->e.equals(userBean.getUsername()));
        if(logical==Logical.AND){
            if((ids.length==0||idStream.allMatch(e->e)) &&
                    (usernames.length==0||userNameStream.allMatch(e->e))){
                throw new AuthorizationException("");
            }
        }else if(logical==Logical.OR){
            if((ids.length==0||idStream.anyMatch(e->e)) &&
                    (usernames.length==0||userNameStream.anyMatch(e->e))){
                throw new AuthorizationException("");
            }
        }

    }

}