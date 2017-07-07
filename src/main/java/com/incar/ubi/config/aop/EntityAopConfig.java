package com.incar.ubi.config.aop;

import com.base.dto.BaseDTO;
import com.incar.icudf.user.dto.SysUserDTO;
import com.incar.ubi.sys.shiro.MyShiroRealm;
import com.incar.ubi.sys.util.ShiroUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
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
     * 切面:所有 DAO层的save开头的方法
     */
    @Pointcut("execution(* com.incar..dao.*DAO.save*(..)) && !execution(* com..dao.LogDAO.save*(..))")
    public void savePointCut(){

    }

    /**
     * 切点:执行保存前的创建时间、创建人、更新时间、更新人设置
     * @param pjp
     */
    @Before("savePointCut()")
    public void doBeforeDAOSave(JoinPoint pjp){
        Object[] paramArr= pjp.getArgs();
        SysUserDTO user= ShiroUtil.getCurrentUser();
        if(user==null){
            return;
        }
        Arrays.stream(paramArr).forEach(param->{
            if(param==null){
                return;
            }
            if(param instanceof BaseDTO){
                setValueBeforeSave((BaseDTO)param,user);
            }else if(param instanceof Iterable){
                Iterator it= ((Iterable) param).iterator();
                while(it.hasNext()){
                    Object obj= it.next();
                    if(obj instanceof BaseDTO){
                        setValueBeforeSave((BaseDTO)obj,user);
                    }
                }
            }
        });
    }

    /**
     * 保存前设置创建人创建时间 更新人更新时间
     * @param baseDTO
     * @param user
     */
    private void setValueBeforeSave(BaseDTO baseDTO,SysUserDTO user){
        Long id= baseDTO.getId();
        if(id==null){
            baseDTO.setCreateTime(new Date());
            if(user!=null){
                baseDTO.setCreateUserId(user.getId());
            }
        }else{
            baseDTO.setUpdateTime(new Date());
            if(user!=null){
                baseDTO.setUpdateUserId(user.getId());
            }
        }
    }
}
