package com.bcd.base.config.redis.schedule.aop;

import com.bcd.base.config.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.base.config.redis.schedule.handler.RedisScheduleHandler;
import com.bcd.base.config.redis.schedule.handler.impl.ClusterFailedScheduleHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bcd on 2018/2/12.
 */
@Aspect
@Component
public class RedisScheduleAopConfig {

    private final static Map<Method, RedisScheduleHandler> METHOD_TO_HANDLER=new ConcurrentHashMap<>();

    private final static Logger logger= LoggerFactory.getLogger(RedisScheduleAopConfig.class);

    private RedisConnectionFactory redisConnectionFactory;

    public RedisScheduleAopConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory=redisConnectionFactory;
    }

    /**
     * 定时任务
     */
    @Pointcut("@annotation(com.bcd.base.config.redis.schedule.anno.ClusterFailedSchedule) || @annotation(com.bcd.base.config.redis.schedule.anno.SingleFailedSchedule)")
    public void methodSchedule(){

    }

    /**
     * 定时任务 环绕通知
     */
    @Around("methodSchedule()")
    public void doAroundSchedule(ProceedingJoinPoint joinPoint){
        //1、获取aop执行的方法
        RedisScheduleHandler handler=null;
        try {
            Method method=getAopMethod(joinPoint);
            handler=METHOD_TO_HANDLER.computeIfAbsent(method,k->{
                ClusterFailedSchedule anno= k.getAnnotation(ClusterFailedSchedule.class);
                return new ClusterFailedScheduleHandler(anno,redisConnectionFactory);
            });
            boolean flag=handler.doBeforeStart();
            if(flag){
                Object[] args = joinPoint.getArgs();
                joinPoint.proceed(args);
                handler.doOnSuccess();
            }
        } catch (Throwable throwable) {
            if(handler!=null){
                handler.doOnFailed();
            }
            logger.error("Cluster Schedule Error",throwable);
        }
    }


    private Method getAopMethod(ProceedingJoinPoint joinPoint) throws Exception{
        //拦截的实体类
        Object target = joinPoint.getTarget();
        //拦截的方法名称
        String methodName = joinPoint.getSignature().getName();
        //拦截的放参数类型
        Class[] parameterTypes = ((MethodSignature)joinPoint.getSignature()).getMethod().getParameterTypes();
        return target.getClass().getMethod(methodName, parameterTypes);

    }


}
