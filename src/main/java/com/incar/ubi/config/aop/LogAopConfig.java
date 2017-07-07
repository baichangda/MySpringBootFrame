package com.incar.ubi.config.aop;

import com.base.dto.BaseDTO;
import com.base.util.ProxyUtil;
import com.base.util.SpringUtil;
import com.incar.ubi.sys.log.bo.LogBO;
import com.incar.ubi.sys.log.dto.LogDTO;
import com.incar.ubi.sys.util.ShiroUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;

/**
 * 日志切面：记录save|delete操作日志
 *
 * @author Aaric
 * @since 2017-05-02
 */
@Aspect
@Component
public class LogAopConfig {

    @Autowired
    private LogBO logBO;

    /**
     * 配置拦截: save方法
     */
    @Pointcut("execution(* com..dao.*DAO.save*(..)) && !execution(* com..dao.LogDAO.save*(..))")
    public void methodSave(){
    }

    /**
     * 配置拦截: delete方法
     */
    @Pointcut("execution(* com..dao.*DAO.delete*(..)) && !execution(* com..dao.LogDAO.delete*(..))")
    public void methodDelete(){
    }

    /**
     * 执行前：保存save方法对象
     *
     * @param joinPoint
     */
    @After("methodSave()")
    public void doAfterMethodSave(JoinPoint joinPoint) throws Exception {
        doLogHandleOnSave(joinPoint);
    }

    /**
     * 执行前：保存delete删除对象
     *
     * @param joinPoint
     */
    @After("methodDelete()")
    public void doAfterMethodDelete(JoinPoint joinPoint) throws Exception {
        doLogHandleOnDelete(joinPoint);
    }

    /**
     * 记录日志
     * 类型：1-新增，2-修改
     *
     * @param joinPoint
     *
     */
    public void doLogHandleOnSave(JoinPoint joinPoint)  throws Exception {
        Object[] paramArr=joinPoint.getArgs();
        Arrays.stream(paramArr).forEach(param->{
            if(param==null){
                return;
            }
            if(param instanceof BaseDTO){
                LogDTO logDTO=new LogDTO();
                if (ShiroUtil.getCurrentUser()!=null){
                    logDTO.setCreateUserId(ShiroUtil.getCurrentUser().getId());
                }
                logDTO.setResourceClass(param.getClass().getName());
                logDTO.setResourceId(((BaseDTO) param).getId());
                if(((BaseDTO) param).getId()==null){
                    logDTO.setOperType(1);
                }else{
                    logDTO.setOperType(2);
                }
                logBO.save(logDTO);
            }else if(param instanceof Iterable){
                Iterator it= ((Iterable) param).iterator();
                while(it.hasNext()){
                    Object obj= it.next();
                    if(obj instanceof BaseDTO){
                        LogDTO logDTO=new LogDTO();
                        if (ShiroUtil.getCurrentUser()!=null) {
                            logDTO.setCreateUserId(ShiroUtil.getCurrentUser().getId());
                        }
                        logDTO.setResourceClass(obj.getClass().getName());
                        logDTO.setResourceId(((BaseDTO) obj).getId());
                        if(((BaseDTO) obj).getId()==null){
                            logDTO.setOperType(1);
                        }else{
                            logDTO.setOperType(2);
                        }
                        logBO.save(logDTO);
                    }
                }
            }
        });
    }


    /**
     * 记录日志
     * 类型  3-删除
     *
     * @param joinPoint
     *
     */
    public void doLogHandleOnDelete(JoinPoint joinPoint)  throws Exception {
        Class clazz= SpringUtil.getSimpleJpaRepositoryBeanClass(ProxyUtil.getSource(joinPoint.getTarget()));
        Object[] paramArr=joinPoint.getArgs();
        Arrays.stream(paramArr).forEach(param->{
            saveDeleteLogForAll(param,clazz);
        });
    }


    /**
     * 根据参数生成删除的log并保存
     * @param param
     * @param clazz
     */
    private void saveDeleteLogForAll(Object param,Class clazz){
        if(param instanceof Iterable){
            Iterator it= ((Iterable) param).iterator();
            while(it.hasNext()) {
                saveDeleteLogForSingle(it.next(),clazz);
            }
        }else if(param.getClass().isArray()){
            Long[] idArr=(Long[])param;
            Arrays.stream(idArr).forEach(id->{
                saveDeleteLogForSingle(id,clazz);
            });
        }else{
            saveDeleteLogForSingle(param,clazz);
        }
    }


    /**
     * 根据参数生成删除的log并保存
     * @param param
     * @param clazz
     */
    private void saveDeleteLogForSingle(Object param,Class clazz){
        if(param==null){
            return;
        }
        if(param instanceof BaseDTO){
            LogDTO logDTO=new LogDTO();
            logDTO.setResourceId(((BaseDTO) param).getId());
            logDTO.setResourceClass(clazz.getName());
            logDTO.setCreateUserId(ShiroUtil.getCurrentUser().getId());
            logDTO.setOperType(3);
            logBO.save(logDTO);
        }else if(param instanceof Long){
            LogDTO logDTO=new LogDTO();
            logDTO.setResourceId((Long)param);
            logDTO.setResourceClass(clazz.getName());
            logDTO.setCreateUserId(ShiroUtil.getCurrentUser().getId());
            logDTO.setOperType(3);
            logBO.save(logDTO);
        }
    }
}
