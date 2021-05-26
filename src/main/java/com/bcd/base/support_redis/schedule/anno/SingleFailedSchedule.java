package com.bcd.base.support_redis.schedule.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleFailedSchedule {
    /**
     * 锁id
     *
     * @return
     */
    String lockId();

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


}
