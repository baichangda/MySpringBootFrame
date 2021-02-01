package com.bcd.base.config.redis.schedule.handler.impl;

import com.bcd.base.config.redis.schedule.anno.SingleFailedSchedule;
import com.bcd.base.config.redis.schedule.handler.RedisScheduleHandler;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.TimeUnit;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 * <p>
 * 原理:
 * 1、到达任务时间点,集群多个实例开始争夺redis锁
 * 2、获取到锁的实例开始执行任务,并设置标记redis锁状态,时间为aliveTime
 * 3、其他实例会直接不执行任务
 */
@SuppressWarnings("unchecked")
public class SingleFailedScheduleHandler extends RedisScheduleHandler {

    private final static Long DEFAULT_ALIVE_TIME = 3000L;


    /**
     * 锁获取后存活时间
     * 单位(毫秒)
     */
    private long aliveTimeInMillis;

    public SingleFailedScheduleHandler(String lockId, RedisConnectionFactory redisConnectionFactory, long aliveTime, TimeUnit aliveTimeUnit) {
        super(lockId,redisConnectionFactory);
        this.aliveTimeInMillis = aliveTimeUnit.toMillis(aliveTime);
    }

    public SingleFailedScheduleHandler(String lockId, RedisConnectionFactory redisConnectionFactory) {
        super(lockId,redisConnectionFactory);
        this.aliveTimeInMillis=DEFAULT_ALIVE_TIME;
    }

    public SingleFailedScheduleHandler(SingleFailedSchedule anno, RedisConnectionFactory redisConnectionFactory) {
        super(anno.lockId(),redisConnectionFactory);
        if(anno.aliveTime()==0L){
            this.aliveTimeInMillis=DEFAULT_ALIVE_TIME;
        }else{
            this.aliveTimeInMillis = anno.aliveTimeUnit().toMillis(anno.aliveTime());
        }
    }

    /**
     * 执行任务之前调用
     * 1、获取锁返回可执行
     *
     * @return
     */
    @Override
    public boolean doBeforeStart() {
        //1、获取锁
        boolean isLock = redisTemplate.execute((RedisConnection connection) ->
                connection.set(keySerializer.serialize(lockId), valueSerializer.serialize("0"), Expiration.milliseconds(aliveTimeInMillis), RedisStringCommands.SetOption.SET_IF_ABSENT)
        );
        return isLock;
    }

    /**
     * 执行失败时调用
     * 什么事情都不做
     */
    @Override
    public void doOnFailed() {
    }

    /**
     * 执行成功时调用
     * 什么事情都不做
     */
    @Override
    public void doOnSuccess() {
    }
}
