package com.bcd.config.redis.schedule.handler.impl;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.config.redis.schedule.anno.ClusterFailedSchedule;
import com.bcd.config.redis.schedule.handler.RedisScheduleHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 集群失败执行模式,如果一个终端执行定时任务失败,会有其他终端执行;直到所有的终端执行失败,定时任务才算失败
 */
public class ClusterFailedScheduleHandler extends RedisScheduleHandler {
    /**
     * 锁失败循环间隔时间(此参数在 单机失败模式下无效 )
     * 获取锁失败时候,循环检测执行结果并重新处理的循环时间间隔
     */
    private long cycleInterval;

    /**
     * 根据随机数生成的各种val
     */
    protected String executingVal;
    protected String successVal;


    public ClusterFailedScheduleHandler(String lockId, long timeOut, long aliveTime, long cycleInterval) {
        super(lockId,timeOut, aliveTime);
        this.cycleInterval = cycleInterval;
        String randomVal= UUID.randomUUID().toString();
        this.executingVal="0-"+randomVal;
        this.successVal="1-"+randomVal;
    }

    public ClusterFailedScheduleHandler(String lockId,long timeOut) {
        this(lockId,timeOut,timeOut/2,timeOut/10);
    }

    public ClusterFailedScheduleHandler(ClusterFailedSchedule anno) {
        this(anno.lockId(),anno.timeOut(),anno.aliveTime()==0L?anno.timeOut()/2:anno.aliveTime(),anno.cycleInterval()==0L?anno.timeOut()/10:anno.cycleInterval());
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
            boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockId, executingVal);
            if (isLock) {
                //2、获取成功则执行过期时间设置
                return doAfterGetLock();
            } else {
                //3、获取锁失败
                /**
                 * 死循环检测修复机制:
                 * 如果当前循环的总时间大于超时时间,则判断锁是否有设置过期时间;如果没有,则直接删除
                 */
                int num=0;
                int maxNum=(int)(timeOut/cycleInterval);
                while (true) {
                    num++;
                    Thread.sleep(cycleInterval);
                    //3.1、获取执行的结果
                    /**
                     * null:执行时间超过超时时间 或者 执行失败
                     * 0:执行中
                     * 1:执行成功
                     */
                    String[] res = parseValue(redisTemplate.opsForValue().get(lockId));
                    if (res[0] == null) {
                        //3.2、如果执行超时或执行失败,此时重新获取锁
                        isLock = redisTemplate.opsForValue().setIfAbsent(lockId, executingVal);
                        if (isLock) {
                            //3.2.1、获取成功则执行过期时间设置
                            return doAfterGetLock();
                        } else {
                            //3.2.2、获取失败则进入下一轮循环等待执行结果
                            continue;
                        }
                    }else if ("0".equals(res[0])) {
                        //3.3、如果正在执行中,判断循环的总时间是否大于设置的超时时间
                        if(num>maxNum){
                            //3.3.1、如果大于,检测当前key的剩余过期时间,避免未设置过期时间死循环
                            Long ttl= redisTemplate.getExpire(lockId, TimeUnit.MILLISECONDS);
                            if(ttl==-1){
                                redisTemplate.delete(lockId);
                            }else{
                                //3.3.2、进入下一轮循环
                                continue;
                            }
                        }else{
                            //3.3.2、否则继续循环
                            continue;
                        }
                    } else if ("1".equals(res[0])) {
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

    /**
     * 在setnx成功后执行
     * 设置过期时间,如果设置失败,释放锁并且返回false
     * @return
     */
    private boolean doAfterGetLock(){
        //1、设置过期时间
        boolean pexpireRes=redisTemplate.expire(lockId, timeOut,TimeUnit.MILLISECONDS);
        //2、如果设置超时时间失败,则不执行;同时释放锁
        if(!pexpireRes){
            redisTemplate.delete(lockId);
            return false;
        }else{
            return true;
        }
    }

    /**
     * 执行成功时调用
     * 1、设置执行成功标志
     * 2、执行成功后设置key存活时间
     *
     * 注意:如果任务执行成功,但是在解锁key和设置存活时间上失败,此时抛出异常;
     * 如果任务存在spring事务,则会进行事务回滚;避免多次执行同一任务
     *
     * 例如:
     * spring容器下可以加上
     * @Transactional(rollbackFor = Exception.class)
     * 且
     * 在catch中抛出运行时异常
     */
    public void doOnSuccess(){
        //1、先判断是否还持有当前锁
        Object res=redisTemplate.opsForValue().get(lockId);
        if(executingVal.equals(res)){
            //1.1、如果持有当前锁,则设置成功标志并设置存活时间
            redisTemplate.opsForValue().set(lockId, successVal, aliveTime, TimeUnit.MILLISECONDS);
        }else{
            //1.2、如果当前锁已经被释放(说明可能有其他终端执行了定时任务),此时抛出异常,让定时任务执行失败
            throw BaseRuntimeException.getException("Other Thread Maybe Execute Task!");
        }
    }

    /**
     * 执行失败时调用
     * 1、执行失败时清除锁,供其他终端执行
     */
    public void doOnFailed() {
        //1、即使删除失败,也没有任何影响,只是会有冗余数据在redis
        redisTemplate.delete(lockId);
    }

    /**
     * 解析redis value结果
     * 第一位
     * 0:执行中
     * 1:执行成功
     * 2:执行失败
     *
     * 第二位
     * 随机数
     *
     * @param val
     * @return
     */
    private String[] parseValue(Object val){
        if(val==null){
            return new String[]{null,null};
        }
        String flag=val.toString().substring(0,1);
        String randomVal=val.toString().substring(2);
        return new String[]{flag,randomVal};
    }
}
