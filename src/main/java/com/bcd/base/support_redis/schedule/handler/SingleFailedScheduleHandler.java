package com.bcd.base.support_redis.schedule.handler;

import com.bcd.base.exception.BaseException;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.support_redis.schedule.anno.SingleFailedSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

/**
 * 单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
 * <p>
 * 原理:
 * 1、到达任务时间点,集群多个实例开始争夺redis锁
 * 2、获取到锁的实例开始执行任务,并设置标记redis锁状态,时间为aliveTime
 * 3、其他实例会直接不执行任务
 */
public class SingleFailedScheduleHandler {

    static Logger logger = LoggerFactory.getLogger(SingleFailedScheduleHandler.class);

    public final static long DEFAULT_ALIVE_TIME = 3000L;

    /**
     * 锁获取后存活时间
     * 单位(毫秒)
     */
    private final Duration aliveTime;
    protected RedisTemplate<String, String> redisTemplate;
    protected ValueOperations<String, String> valueOperations;
    /**
     * 定时任务的锁表示字符串,确保每一个定时任务设置不同的锁id
     */
    protected String lockId;

    public SingleFailedScheduleHandler(String lockId, RedisConnectionFactory redisConnectionFactory, Duration aliveTime) {
        this.lockId = lockId;
        this.redisTemplate = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
        this.valueOperations = this.redisTemplate.opsForValue();
        this.aliveTime = aliveTime;
    }

    public SingleFailedScheduleHandler(SingleFailedSchedule anno, RedisConnectionFactory redisConnectionFactory) {
        this(anno.lockId(), redisConnectionFactory, Duration.ofMillis(anno.aliveTime()));
    }

    /**
     * 执行任务之前调用
     *
     * @return
     */
    public boolean doBeforeStart() {
        //1、获取锁
        Boolean res = valueOperations.setIfAbsent(lockId, "0", aliveTime);
        if (res == null) {
            throw BaseException.get("doBeforeStart lockId[{}] res null", lockId);
        }
        return res;
    }

    /**
     * 执行失败时调用
     * 什么事情都不做
     */
    public void doOnFailed() {
    }

    /**
     * 执行成功时调用
     * 什么事情都不做
     */
    public void doOnSuccess() {
    }
}
