package com.bcd.base.redis.schedule.aop;

import com.bcd.base.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.base.redis.schedule.anno.SingleFailedSchedule;
import com.bcd.base.redis.schedule.handler.RedisScheduleHandler;
import com.bcd.base.redis.schedule.handler.impl.ClusterFailedScheduleHandler;
import com.bcd.base.redis.schedule.handler.impl.SingleFailedScheduleHandler;
import com.bcd.base.exception.BaseRuntimeException;
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

    private final static Map<Method, RedisScheduleHandler> METHOD_TO_HANDLER = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(RedisScheduleAopConfig.class);

    private RedisConnectionFactory redisConnectionFactory;

    public RedisScheduleAopConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 定时任务
     */
    @Pointcut("@annotation(com.bcd.base.redis.schedule.anno.ClusterFailedSchedule) " +
            "|| @annotation(com.bcd.base.redis.schedule.anno.SingleFailedSchedule)")
    public void methodSchedule() {

    }

    /**
     * 定时任务 环绕通知
     */
    @Around("methodSchedule()")
    public void doAroundSchedule(ProceedingJoinPoint joinPoint) {
        //1、获取aop执行的方法
        Method method = getAopMethod(joinPoint);
        RedisScheduleHandler handler = METHOD_TO_HANDLER.computeIfAbsent(method, k -> {
            SingleFailedSchedule anno1 = method.getAnnotation(SingleFailedSchedule.class);
            if (anno1 == null) {
                ClusterFailedSchedule anno2 = method.getAnnotation(ClusterFailedSchedule.class);
                return new ClusterFailedScheduleHandler(anno2, redisConnectionFactory);
            } else {
                return new SingleFailedScheduleHandler(anno1, redisConnectionFactory);
            }
        });
        boolean flag = handler.doBeforeStart();
        if (flag) {
            Object[] args = joinPoint.getArgs();
            try {
                joinPoint.proceed(args);
            } catch (Throwable throwable) {
                handler.doOnFailed();
                logger.error("schedule error", throwable);
            }
            handler.doOnSuccess();
        }
    }


    private Method getAopMethod(ProceedingJoinPoint joinPoint) {
        //拦截的实体类
        Object target = joinPoint.getTarget();
        //拦截的方法名称
        String methodName = joinPoint.getSignature().getName();
        //拦截的放参数类型
        Class[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
        try {
            return target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }


}
