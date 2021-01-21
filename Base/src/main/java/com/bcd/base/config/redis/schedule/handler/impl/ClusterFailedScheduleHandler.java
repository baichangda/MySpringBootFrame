package com.bcd.base.config.redis.schedule.handler.impl;

import com.bcd.base.config.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.base.config.redis.schedule.handler.RedisScheduleHandler;
import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.TimeUnit;

/**
 * 集群失败执行模式,如果一个终端执行定时任务失败,会有其他终端执行;直到所有的终端执行失败,定时任务才算失败
 * <p>
 * 原理:
 * 1、到达任务时间点,集群中多个实例开始争夺redis锁,锁具有超时时间为timeOut
 * 2、获取到redis锁的实例开始执行任务,其他未获取到锁的实例循环等待,循环的间隔为cycleInterval
 * 3、如果执行成功,将成功标记设置到redis中,其他实例检测到成功的结果,结束循环
 * 4、如果执行失败,清除redis锁,此实例不再参与锁竞争,其他等待的实例进行锁竞争
 * 5、如果执行超时,此时锁会被redis自动清除,其他实例会进行锁竞争
 * <p>
 * 所以:
 * timeOut必须大于 任务成功执行时间,否则会出现任务被执行多次
 * aliveTime必须大于 cycleInterval,否则会出现即使执行成功其他实例也检测不到结果,出现执行多次
 *
 */
@SuppressWarnings("unchecked")
public class ClusterFailedScheduleHandler extends RedisScheduleHandler {

    private final static Logger logger = LoggerFactory.getLogger(ClusterFailedScheduleHandler.class);

    /**
     * 任务执行超时时间(请确保任务执行时间不会超过此时间)
     * 在指定超时时间之后,无论任务是否执行完毕都会释放锁
     * 单位(毫秒)
     */
    private long timeOut;

    /**
     * 任务执行后锁存活时间
     * 在任务执行后为了让其他终端检测到执行结果,并作出相应的反应
     * 单位(毫秒)
     */
    private long aliveTime;

    /**
     * 锁失败循环间隔时间
     * 获取锁失败时候,循环检测执行结果并重新处理的循环时间间隔
     * 单位(毫秒)
     */
    private long cycleInterval;

    /**
     * 根据随机数生成的各种val
     */
    protected String executingVal="0";
    protected String successVal="1";


    public ClusterFailedScheduleHandler(String lockId, RedisConnectionFactory redisConnectionFactory, long timeOut, long aliveTime, long cycleInterval) {
        super(lockId,redisConnectionFactory);
        this.timeOut = timeOut;
        this.aliveTime = aliveTime;
        this.cycleInterval = cycleInterval;

    }

    public ClusterFailedScheduleHandler(String lockId, RedisConnectionFactory redisConnectionFactory, long timeOut) {
        this(lockId,redisConnectionFactory, timeOut, timeOut / 2, timeOut / 10);
    }

    public ClusterFailedScheduleHandler(ClusterFailedSchedule anno, RedisConnectionFactory redisConnectionFactory) {
        this(anno.lockId(),redisConnectionFactory, anno.timeOut(), anno.aliveTime() == 0L ? anno.timeOut() / 2 : anno.aliveTime(), anno.cycleInterval() == 0L ? anno.timeOut() / 10 : anno.cycleInterval());
    }

    /**
     * 执行任务前调用
     * 1、获取锁,若获取失败则循环等待其他终端的执行结果;
     * 若其他终端执行成功,则终止本次任务执行
     * 若其他终端执行失败,则继续获取锁来执行本次任务,如此循环
     *
     * @return
     */
    public boolean doBeforeStart() {
        try {
            //获取锁
            boolean isLock = getLock();
            if (isLock) {
                //获取成功则执行任务
                return true;
            } else {
                while(true){
                    Thread.sleep(cycleInterval);
                    /**
                     * null:执行时间超过超时时间 或者 执行失败
                     * 0:执行中
                     * 1:执行成功
                     */
                    String val=redisTemplate.opsForValue().get(lockId);
                    if(val==null){
                        //如果执行超时或执行失败,此时重新获取锁
                        isLock = getLock();
                        if (isLock) {
                            //获取成功则执行任务
                            return true;
                        }
                    }else{
                        if(val.equals("1")){
                            //如果执行完成,则当前机器不执行本次任务
                            return false;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.error("Cluster Schedule Interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取锁
     *
     * @return
     */
    private boolean getLock() {
        return redisTemplate.execute((RedisConnection connection) ->
                connection.set(keySerializer.serialize(lockId), valueSerializer.serialize(executingVal), Expiration.milliseconds(timeOut), RedisStringCommands.SetOption.SET_IF_ABSENT)
        );
    }

    /**
     * 执行成功时调用
     * 1、设置执行成功标志
     * 2、执行成功后设置key存活时间
     * <p>
     * 注意:如果任务执行成功,但是在解锁key和设置存活时间上失败,此时抛出异常;
     * 如果任务存在spring事务,则会进行事务回滚;避免多次执行同一任务
     * <p>
     * 例如:
     * spring容器下可以加上
     *
     * @Transactional(rollbackFor = Exception.class)
     * 且
     * 在catch中抛出运行时异常
     */
    public void doOnSuccess() {
        //先判断是否还持有当前锁
        Object res = redisTemplate.opsForValue().get(lockId);
        if (executingVal.equals(res)) {
            //如果持有当前锁,则设置成功标志并设置存活时间
            redisTemplate.opsForValue().set(lockId, successVal, aliveTime, TimeUnit.MILLISECONDS);
        } else {
            //如果当前锁已经被释放(说明可能有其他终端执行了定时任务),此时抛出异常,让定时任务执行失败
            throw BaseRuntimeException.getException("Other Thread Maybe Execute Task!");
        }
    }

    /**
     * 执行失败时调用
     * 1、执行失败时清除锁,供其他终端执行
     */
    public void doOnFailed() {
        //即使删除失败,也没有任何影响,只是会有冗余数据在redis
        redisTemplate.delete(lockId);
    }

}
