package com.bcd.config.redis.schedule.handler.impl;

import com.bcd.config.redis.schedule.handler.RedisScheduleClusterHandler;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 */
public class SingleFailedScheduleHandler extends RedisScheduleClusterHandler {

    public SingleFailedScheduleHandler(String lockId, long timeOut,long aliveTime) {
        super(lockId, timeOut,aliveTime);
    }

    public SingleFailedScheduleHandler(String lockId, long timeOut) {
        this(lockId, timeOut,2000L);
    }

    /**
     * 执行任务之前调用
     * 1、获取锁返回可执行
     * @return
     */
    @Override
    public boolean doBeforeStart() {
        //1、获取锁
        Long lock = redisOp.setnx(lockId, "0");
        if (lock == 1L) {
            //2、获取锁成功则设置过期时间
            redisOp.pexpire(lockId, timeOut);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 执行失败时调用
     * 1、设置执行失败标志
     * 2、执行失败后设置key存活时间
     */
    @Override
    public void doOnFailed() {
        redisOp.set(lockId, "2");
        redisOp.pexpire(lockId, aliveTime);
    }

    /**
     * 执行成功时调用
     * 1、设置执行成功标志
     * 2、执行成功后设置key存活时间
     */
    @Override
    public void doOnSuccess() {
        redisOp.set(lockId, "1");
        redisOp.pexpire(lockId, aliveTime);
    }
}
