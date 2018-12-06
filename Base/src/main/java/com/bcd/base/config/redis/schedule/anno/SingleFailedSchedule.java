package com.bcd.base.config.redis.schedule.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleFailedSchedule {
    /**
     * 锁id
     * @return
     */
    String lockId();

    /**
     * 任务执行完毕key存活时间
     * 单位(毫秒)
     * @return
     */
    long aliveTime() default 0L;
}
