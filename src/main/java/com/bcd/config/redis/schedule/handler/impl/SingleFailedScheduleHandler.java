package com.bcd.config.redis.schedule.handler.impl;

import com.bcd.config.redis.schedule.anno.SingleFailedSchedule;
import com.bcd.config.redis.schedule.handler.RedisScheduleHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 */
@SuppressWarnings("unchecked")
public class SingleFailedScheduleHandler extends RedisScheduleHandler {

    private final static Long DEFAULT_ALIVE_TIME=2000L;

    /**
     * 任务执行超时时间(请确保任务执行时间不会超过此时间)
     * 在指定超时时间之后,无论任务是否执行完毕都会释放锁
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
            boolean isLock=(boolean)redisTemplate.execute(new RedisCallback<Object>() {
                @Nullable
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    return connection.set(redisTemplate.getKeySerializer().serialize(lockId),redisTemplate.getValueSerializer().serialize("0"), Expiration.milliseconds(aliveTime), RedisStringCommands.SetOption.SET_IF_ABSENT);
                }
            });
            return isLock;
        }catch (Exception e){
            e.printStackTrace();
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
