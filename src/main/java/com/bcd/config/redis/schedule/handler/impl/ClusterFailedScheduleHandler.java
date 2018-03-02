package com.bcd.config.redis.schedule.handler.impl;

import com.bcd.config.redis.schedule.handler.RedisScheduleClusterHandler;

/**
 * 集群失败执行模式,如果一个终端执行定时任务失败,会有其他终端执行;直到所有的终端执行失败,定时任务才算失败
 */
public class ClusterFailedScheduleHandler extends RedisScheduleClusterHandler {
    /**
     * 锁失败循环间隔时间(此参数在 单机失败模式下无效 )
     * 获取锁失败时候,循环检测执行结果并重新处理的循环时间间隔
     */
    private long cycleInterval;


    public ClusterFailedScheduleHandler(String lockId, long timeOut, long aliveTime, long cycleInterval) {
        super(lockId, timeOut, aliveTime);
        this.cycleInterval = cycleInterval;
    }

    public ClusterFailedScheduleHandler(String lockId, long timeOut) {
        this(lockId,timeOut,timeOut/2,timeOut/10);
    }

    /**
     * 执行成功时调用
     * 1、设置执行成功标志
     * 2、执行成功后设置key存活时间
     */
    public void doOnSuccess() {
        redisOp.set(lockId, "1");
        redisOp.pexpire(lockId, aliveTime);
    }

    /**
     * 执行失败时调用
     * 1、执行失败时清除锁,供其他终端执行
     */
    public void doOnFailed() {
        redisOp.del(lockId);
    }


    /**
     * 执行任务前调用
     * 1、获取锁,若获取失败则循环等待其他终端的执行结果;
     *   若其他终端执行成功,则终止本次任务执行
     *   若其他终端执行失败,则继续获取锁来执行本次任务,如此循环
     * @return
     */
    public boolean doBeforeStart() {
        try {
            //1、获取锁
            Long lock = redisOp.setnx(lockId, "0");
            if (lock == 1L) {
                //2、获取锁成功则设置过期时间
                redisOp.pexpire(lockId, timeOut);
                return true;
            } else {
                //3、获取锁失败
                while (true) {
                    Thread.sleep(cycleInterval);
                    //3.1、获取执行的结果
                    /**
                     * null:执行时间超过超时时间 或者 执行失败
                     * 0:执行中
                     * 1:执行成功
                     */
                    String res = redisOp.get(lockId);
                    if (res == null) {
                        //3.2、如果执行超时,此时重新获取锁
                        lock = redisOp.setnx(lockId, "0");
                        if (lock == 1L) {
                            //3.2.1、获取成功则设置过期时间
                            redisOp.pexpire(lockId, timeOut);
                            return true;
                        } else {
                            //3.2.2、获取失败则进入下一轮循环等待执行结果
                            continue;
                        }
                    }else if ("0".equals(res)) {
                        //3.3、如果正在执行中
                        //3.3.1、检测当前key的剩余过期时间,如果未设置,则说明异常死循环,此时设置过期时间
                        Long ttl= redisOp.ttl(lockId);
                        if(ttl==-1){
                            redisOp.pexpire(lockId,timeOut);
                        }else{
                            //3.3.2、进入下一轮循环
                            continue;
                        }
                    } else if ("1".equals(res)) {
                        //3.4、如果执行完成,则当前机器不执行本次任务
                        return false;
                    } else {
                        //3.5、如果出现其他未知的结果,则直接不允许执行
                        return false;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
