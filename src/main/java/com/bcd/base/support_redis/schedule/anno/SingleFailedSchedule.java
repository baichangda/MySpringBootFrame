package com.bcd.base.support_redis.schedule.anno;

import com.bcd.base.support_redis.schedule.handler.SingleFailedScheduleHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * 任务执行完毕key存活时间(ms)
     *
     * @return
     */
    long aliveTime() default SingleFailedScheduleHandler.DEFAULT_ALIVE_TIME;
}
