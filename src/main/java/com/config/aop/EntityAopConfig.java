package com.config.aop;

import com.base.dto.BaseBean;
import com.sys.bean.UserBean;
import com.sys.util.ShiroUtil;
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
@Aspect
@Component
public class EntityAopConfig {
    /**
     * 切面:所有 Repository 层的save开头的方法
     */
    @Pointcut("execution(* com..repository.*Repository.save*(..)) && !execution(* com..repository.LogRepository.save*(..))")
    public void savePointCut(){

    }

    /**
     * 切点:执行保存前的创建时间、创建人、更新时间、更新人设置
     * @param pjp
     */
    @Before("savePointCut()")
    public void doBeforeDAOSave(JoinPoint pjp){
        Object[] paramArr= pjp.getArgs();
        UserBean user= ShiroUtil.getCurrentUser();
        if(user==null){
            return;
        }
        Arrays.stream(paramArr).forEach(param->{
            if(param==null){
                return;
            }
            if(param instanceof BaseBean){
                setValueBeforeSave((BaseBean)param,user);
            }else if(param instanceof Iterable){
                Iterator it= ((Iterable) param).iterator();
                while(it.hasNext()){
                    Object obj= it.next();
                    if(obj instanceof BaseBean){
                        setValueBeforeSave((BaseBean)obj,user);
                    }
                }
            }
        });
    }

    /**
     * 保存前设置创建人创建时间 更新人更新时间
     * @param baseBean
     * @param user
     */
    private void setValueBeforeSave(BaseBean baseBean, UserBean user){
        Long id= baseBean.getId();
        if(id==null){
            baseBean.setCreateTime(new Date());
            if(user!=null){
                baseBean.setCreateUserId(user.getId());
            }
        }else{
            baseBean.setUpdateTime(new Date());
            if(user!=null){
                baseBean.setUpdateUserId(user.getId());
            }
        }
    }
}
