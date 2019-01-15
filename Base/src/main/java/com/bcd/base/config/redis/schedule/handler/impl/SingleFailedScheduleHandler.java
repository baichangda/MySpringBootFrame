package com.bcd.base.config.redis.schedule.handler.impl;

import com.bcd.base.config.redis.schedule.anno.SingleFailedSchedule;
import com.bcd.base.config.redis.schedule.handler.RedisScheduleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 *
 * 原理:
 * 1、到达任务时间点,集群多个实例开始争夺redis锁
 * 2、获取到锁的实例开始执行任务,并设置标记redis锁状态,时间为aliveTime
 * 3、其他实例会直接不执行任务
 */
@SuppressWarnings("unchecked")
public class SingleFailedScheduleHandler extends RedisScheduleHandler {

    private final static Logger logger= LoggerFactory.getLogger(SingleFailedScheduleHandler.class);

    private final static Long DEFAULT_ALIVE_TIME=2000L;

    /**
     * 锁获取后存活时间
     * 单位(毫秒)
     */
    private long aliveTime;

    public SingleFailedScheduleHandler(String lockId,long aliveTime) {
        super(lockId);
        this.aliveTime=aliveTime;
    }

    public SingleFailedScheduleHandler(String lockId) {
        this(lockId,DEFAULT_ALIVE_TIME);
    }

    public SingleFailedScheduleHandler(SingleFailedSchedule anno){
        this(anno.lockId(),anno.aliveTime()==0L?DEFAULT_ALIVE_TIME:anno.aliveTime());
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
            boolean isLock=(boolean)redisTemplate.execute((RedisConnection connection)->
                    connection.set(redisTemplate.getKeySerializer().serialize(lockId),redisTemplate.getValueSerializer().serialize("0"), Expiration.milliseconds(aliveTime), RedisStringCommands.SetOption.SET_IF_ABSENT)
            );
            return isLock;
        }catch (Exception e){
            logger.error("Error",e);
            return false;
        }
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
