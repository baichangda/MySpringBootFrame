package com.bcd.base.config.redis.schedule.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClusterFailedSchedule {
    /**
     * 锁id
     *
     * @return
     */
    String lockId();

    /**
     * 超时时间
     *
     * @return
     */
    long timeout();

    /**
     * 时间单位
     *
     * @return
     */
    TimeUnit timeoutUnit() default TimeUnit.SECONDS;

    /**
     * 任务执行完毕key存活时间
     *
     * @return
     */
    long aliveTime() default 0L;

    /**
     * 时间单位
     *
     * @return
     */
    TimeUnit aliveTimeUnit() default TimeUnit.SECONDS;

    /**
     * 获取key失败循环周期
     * 单位(毫秒)
     *
     * @return
     */
    long cycleInterval() default 0L;

    /**
     * 时间单位
     *
     * @return
     */
    TimeUnit cycleIntervalUnit() default TimeUnit.SECONDS;
}
