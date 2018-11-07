package com.bcd.config.aop;

import com.bcd.base.util.IPUtil;
import com.bcd.rdb.bean.BaseBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.util.ShiroUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
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
     * 切面:所有 Repository 层的save开头的方法
     */
    @Pointcut("execution(* com.bcd..repository.*Repository.save*(..)) && !execution(* com.bcd..repository.LogRepository.save*(..))")
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
        Arrays.stream(paramArr).forEach(param->{
            if(param==null){
                return;
            }
            if(param instanceof BaseBean){
                setValueBeforeSave((BaseBean) param,user);
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
     * @param bean
     * @param user
     */
    private void setValueBeforeSave(BaseBean bean, UserBean user){
        //1、判断主键id是否为null,因此判断其为新增还是修改
        Object id=bean.getId();
        //2、属性注入
        if(id==null){
            bean.setCreateTime(new Date());
            HttpServletRequest request=getRequest();
            if(request!=null){
                bean.setCreateIp(IPUtil.getIpAdrress(request));
            }
            if(user!=null){
                bean.setCreateUserId(user.getId());
                bean.setCreateUserName(user.getUsername());

            }
        }else{
            bean.setUpdateTime(new Date());
            HttpServletRequest request=getRequest();
            if(request!=null){
                bean.setUpdateIp(IPUtil.getIpAdrress(request));
            }
            if(user!=null){
                bean.setUpdateUserId(user.getId());
                bean.setUpdateUserName(user.getUsername());
            }
        }
    }

    private HttpServletRequest getRequest(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes==null?null:servletRequestAttributes.getRequest();
    }
}
