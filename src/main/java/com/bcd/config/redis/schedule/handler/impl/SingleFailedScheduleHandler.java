package com.bcd.config.redis.schedule.handler.impl;

import com.bcd.config.redis.schedule.anno.SingleFailedSchedule;
import com.bcd.config.redis.schedule.handler.RedisScheduleHandler;

import java.util.concurrent.TimeUnit;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 */
public class SingleFailedScheduleHandler extends RedisScheduleHandler {

    private final static Long DEFAULT_ALIVE_TIME=2000L;

    public SingleFailedScheduleHandler(String lockId,long timeOut,long aliveTime) {
        super(lockId,timeOut,aliveTime);
    }

    public SingleFailedScheduleHandler(String lockId,long timeOut) {
        this(lockId,timeOut,DEFAULT_ALIVE_TIME);
    }

    public SingleFailedScheduleHandler(SingleFailedSchedule anno){
        this(anno.lockId(),anno.timeOut(),anno.aliveTime()==0L?DEFAULT_ALIVE_TIME:anno.aliveTime());
    }
    /**
     * 执行任务之前调用
     * 1、获取锁返回可执行
     * @return
     */
    @Override
    public boolean doBeforeStart() {
        try {
            //1、获取锁
            boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockId, "0");
            if (isLock) {
                //2、获取锁成功则设置过期时间
                boolean pexpireRes=redisTemplate.expire(lockId, timeOut, TimeUnit.MILLISECONDS);
                //2.1、如果设置失败,则不执行;同时释放锁
                if(!pexpireRes){
                    redisTemplate.delete(lockId);
                    return false;
                }else{
                    return true;
                }
            } else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
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
        //1、即使设置失败,也没有任何影响,只是会有冗余数据在redis
        redisTemplate.opsForValue().set(lockId,"2",aliveTime,TimeUnit.MILLISECONDS);
    }

    /**
     * 执行成功时调用
     * 1、设置执行成功标志
     * 2、执行成功后设置key存活时间
     *
     */
    @Override
    public void doOnSuccess() {
        //1、即使设置失败,也没有任何影响,只是会有冗余数据在redis
        redisTemplate.opsForValue().set(lockId,"1",aliveTime,TimeUnit.MILLISECONDS);
    }
}
