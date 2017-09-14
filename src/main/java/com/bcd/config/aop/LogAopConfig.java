package com.bcd.config.aop;

import com.base.util.ProxyUtil;
import com.base.util.SpringUtil;
import com.bcd.rdb.bean.BaseBean;
import com.bcd.rdb.util.RDBUtil;
import com.sys.bean.LogBean;
import com.sys.service.LogService;
import com.sys.util.ShiroUtil;
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
    private LogService logService;

    /**
     * 配置拦截: save方法
     */
    @Pointcut("execution(* com..repository.*Repository.save*(..)) && !execution(* com..repository.LogRepository.save*(..))")
    public void methodSave(){
    }

    /**
     * 配置拦截: delete方法
     */
    @Pointcut("execution(* com..repository.*Repository.delete*(..)) && !execution(* com..repository.LogRepository.delete*(..))")
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
            if(param instanceof BaseBean){
                LogBean log=new LogBean();
                if (ShiroUtil.getCurrentUser()!=null){
                    log.setCreateUserId(ShiroUtil.getCurrentUser().getId());
                }
                log.setResourceClass(param.getClass().getName());
                log.setResourceId(((BaseBean) param).getId());
                if(((BaseBean) param).getId()==null){
                    log.setOperType(1);
                }else{
                    log.setOperType(2);
                }
                logService.save(log);
            }else if(param instanceof Iterable){
                Iterator it= ((Iterable) param).iterator();
                while(it.hasNext()){
                    Object obj= it.next();
                    if(obj instanceof BaseBean){
                        LogBean log=new LogBean();
                        if (ShiroUtil.getCurrentUser()!=null) {
                            log.setCreateUserId(ShiroUtil.getCurrentUser().getId());
                        }
                        log.setResourceClass(obj.getClass().getName());
                        log.setResourceId(((BaseBean) obj).getId());
                        if(((BaseBean) obj).getId()==null){
                            log.setOperType(1);
                        }else{
                            log.setOperType(2);
                        }
                        logService.save(log);
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
        Class clazz= RDBUtil.getSimpleJpaRepositoryBeanClass(ProxyUtil.getSource(joinPoint.getTarget()));
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
        if(param instanceof BaseBean){
            LogBean log=new LogBean();
            log.setResourceId(((BaseBean) param).getId());
            log.setResourceClass(clazz.getName());
            log.setCreateUserId(ShiroUtil.getCurrentUser().getId());
            log.setOperType(3);
            logService.save(log);
        }else if(param instanceof Long){
            LogBean logDTO=new LogBean();
            logDTO.setResourceId((Long)param);
            logDTO.setResourceClass(clazz.getName());
            logDTO.setCreateUserId(ShiroUtil.getCurrentUser().getId());
            logDTO.setOperType(3);
            logService.save(logDTO);
        }
    }
}
