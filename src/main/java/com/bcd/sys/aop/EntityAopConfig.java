package com.bcd.sys.aop;

import com.bcd.base.support_jdbc.bean.BaseBean;
import com.bcd.base.support_satoken.SaTokenUtil;
import com.bcd.sys.bean.UserBean;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/4/13.
 */
@SuppressWarnings("unchecked")
@Aspect
@Component
public class EntityAopConfig {

    /**
     * 切面:
     * 1、所有 Service 层的save方法
     */
    @Pointcut("execution(* com.bcd..service.*Service.save(..))")
    public void savePointCut() {

    }

    /**
     * 切点:执行保存前的创建时间、创建人、更新时间、更新人设置
     *
     * @param pjp
     */
    @Before("savePointCut()")
    public void doBeforeDAOSave(JoinPoint pjp) {
        Object[] paramArr = pjp.getArgs();
        UserBean user = SaTokenUtil.getLoginUser_cache();
        Arrays.stream(paramArr).forEach(param -> {
            if (param == null) {
                return;
            }
            if (param instanceof BaseBean) {
                setValueBeforeSave((BaseBean) param, user);
            } else if (param instanceof Iterable) {
                Iterator it = ((Iterable) param).iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    if (obj instanceof BaseBean) {
                        setValueBeforeSave((BaseBean) obj, user);
                    }
                }
            }
        });
    }

    /**
     * 保存前设置创建人创建时间 更新人更新时间
     *
     * @param bean
     * @param user
     */
    private void setValueBeforeSave(BaseBean bean, UserBean user) {
        //1、判断主键id是否为null,因此判断其为新增还是修改
        Object id = bean.id;
        //2、属性注入
        if (id == null) {
            bean.createTime=new Date();
            if (user != null) {
                bean.createUserId=user.id;
                bean.createUserName=user.realName;

            }
        } else {
            bean.updateTime=new Date();
            if (user != null) {
                bean.updateUserId=user.id;
                bean.updateUserName=user.realName;
            }
        }
    }
}
