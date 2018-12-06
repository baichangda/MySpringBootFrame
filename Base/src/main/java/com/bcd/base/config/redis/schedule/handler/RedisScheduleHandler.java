package com.bcd.base.config.redis.schedule.handler;

import com.bcd.base.util.SpringUtil;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * Redis定时任务集群处理类
 *
 * 在定时任务执行之前,调用 doBeforeStart 来获取当前集群定时任务的锁
 * 在定时任务执行成功之后,调用 doOnSuccess 来标示当前定时任务已经执行成功,其他终端定时刷新定时任务执行结果来 销毁自己的定时任务
 * 在定时任务执行失败之后,调用 doOnFailed 来释放锁,供其他终端来获取锁执行定时任务
 *
 * 注意:
 * 1、同一个lockId的定时任务配置必须一样,否则可能导致未知的错误
 * 2、此处理类不适用于频率非常高的定时任务,建议使用在定时任务周期>30s(具体的任务时间周期受redis网络环境的影响)
 *
 */
public abstract class RedisScheduleHandler {


    protected RedisTemplate redisTemplate;
    /**
     * 定时任务的锁表示字符串,确保每一个定时任务设置不同的锁id
     */
    protected String lockId;

    public RedisScheduleHandler(String lockId){
        this.lockId= lockId;
        this.redisTemplate=SpringUtil.applicationContext.getBean(RedisTemplate.class);
    }




    /**
     * 开始执行定时任务之前调用
     * @return true代表可以执行 false代表不执行
     */
    public abstract boolean doBeforeStart();

    /**
     * 执行失败时调用
     */
    public abstract void doOnFailed();

    /**
     * 执行成功时调用
     * 注意:
     * 如果是ClusterFailedScheduleHandler的实现类,必须处理异常回滚,防止任务被多次执行
     */
    public abstract void doOnSuccess();

}
