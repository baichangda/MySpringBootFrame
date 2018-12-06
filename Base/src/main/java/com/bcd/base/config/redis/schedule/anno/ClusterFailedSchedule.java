package com.bcd.base.config.redis.schedule.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClusterFailedSchedule {
    /**
     * 锁id
     * @return
     */
    String lockId();

    /**
     * 超时时间
     * 单位(毫秒)
     * @return
     */
    long timeOut();

    /**
     * 任务执行完毕key存活时间
     * 单位(毫秒)
     * @return
     */
    long aliveTime() default 0L;

    /**
     * 获取key失败循环周期
     * 单位(毫秒)
     * @return
     */
    long cycleInterval() default 0L;
}
