package com.bcd.config.redis.schedule;

import com.bcd.base.util.SpringUtil;
import redis.clients.jedis.JedisCluster;

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
public class RedisScheduleClusterHandler {
    private JedisCluster redisOp;
    /**
     * 任务执行超时时间(请确保任务执行时间不会超过此时间)
     * 在指定超时时间之后,无论任务是否执行完毕都会释放锁
     */
    private long timeOut;
    /**
     * 锁失败循环间隔时间(此参数在 单机失败模式下无效 )
     * 获取锁失败时候,循环检测执行结果并重新处理的循环时间间隔
     */
    private long cycleInterval;
    /**
     * 任务执行成功后锁存活时间
     * 在任务执行成功后,为了让其他正在等待获取锁终端销毁自己的定时任务
     * 注意:在集群失败模式下,此时间必须大于cycleInterval,否则会导致定时任务多次执行
     */
    private long aliveTime;

    /**
     * 定时任务的锁表示字符串,确保每一个定时任务设置不同的锁id
     */
    private String lockId;

    /**
     *  1:
     *  单机失败执行模式,只会有一个终端执行定时任务,结果取决于这个终端执行结果
     *  2:
     *  集群失败执行模式,如果一个终端执行定时任务失败,会有其他终端执行;直到所有的终端执行失败,定时任务才算失败
     */
    private int mode;

    public RedisScheduleClusterHandler(String lockId, long timeOut, long cycleInterval, long aliveTime,int mode) {
        this.lockId = lockId;
        this.timeOut = timeOut;
        this.cycleInterval = cycleInterval;
        this.aliveTime = aliveTime;
        this.redisOp = SpringUtil.applicationContext.getBean(JedisCluster.class);
        this.mode=mode;
    }

    public RedisScheduleClusterHandler(String lockId, long timeOut, long cycleInterval, long aliveTime){
        this(lockId, timeOut, cycleInterval, aliveTime,1);
    }

    public RedisScheduleClusterHandler(String lockId, long timeOut) {
        this(lockId, timeOut, timeOut / 10, timeOut / 2);
    }

    public RedisScheduleClusterHandler(String lockId, long timeOut,int mode) {
        this(lockId, timeOut, timeOut / 10, timeOut / 2,mode);
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public long getCycleInterval() {
        return cycleInterval;
    }

    public void setCycleInterval(long cycleInterval) {
        this.cycleInterval = cycleInterval;
    }

    public long getAliveTime() {
        return aliveTime;
    }

    public void setAliveTime(long aliveTime) {
        this.aliveTime = aliveTime;
    }

    public JedisCluster getRedisOp() {
        return redisOp;
    }

    public void setRedisOp(JedisCluster redisOp) {
        this.redisOp = redisOp;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 开始执行定时任务之前调用
     * @return true代表可以执行 false代表不执行
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
                //3.1、如果当前模式是单机失败模式,则直接返回失败;
                if(mode==1){
                    return false;
                }
                while (true) {
                    Thread.sleep(cycleInterval);
                    //3.1、获取执行的结果
                    /**
                     * null:执行时间超过超时时间 或者 执行失败(在集群失败模式下 执行失败 返回null)
                     * 0:执行中
                     * 1:执行成功
                     * 2:执行失败(在单机失败模式下 才会出现此结果)
                     */
                    String res = redisOp.get(lockId);
                    if (res == null) {
                        //3.2、如果执行超时,此时重新获取锁
                        lock = redisOp.setnx(lockId, "0");
                        if (lock == 1L) {
                            //获取成功则设置过期时间
                            redisOp.pexpire(lockId, timeOut);
                            return true;
                        } else {
                            continue;
                        }
                    } else if ("0".equals(res)) {
                        //3.3、如果正在执行中,则进行下一个循环等待
                        continue;
                    } else if ("1".equals(res)) {
                        //3.4、如果执行完成,则当前机器不执行本次任务
                        return false;
                    } else {
                        return false;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
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
     */
    public void doOnFailed() {
        /**
         * 如果是单机失败模式,则不能删除,必须等待自动过期
         * 因为有可能出现 某个终端还没有来得及获取锁 , 另一个终端定时任务就已经执行完毕
         */
        if(mode==1){
            redisOp.set(lockId, "2");
            redisOp.pexpire(lockId, aliveTime);
        }else if(mode==2){
            redisOp.del(lockId);
        }
    }
}
