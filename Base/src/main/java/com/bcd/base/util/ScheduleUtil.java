package com.bcd.base.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@SuppressWarnings("unchecked")
public class ScheduleUtil {
    private final static Map<String,ScheduledFuture> ID_TO_FUTURE=new ConcurrentHashMap<>();

    private static String storeFuture(ScheduledFuture future){
        String id= RandomStringUtils.randomAlphanumeric(32);
        ID_TO_FUTURE.put(id,future);
        return id;
    }

    /**
     * 根据cron表达式启动定时任务
     * @param runnable 任务
     * @param cron cron表达式
     * @param timeZone 时区
     * @return
     */
    public static String scheduleByCron(Runnable runnable, String cron, TimeZone timeZone){
        CronTrigger cronTrigger=new CronTrigger(cron,timeZone);
        ScheduledFuture future= Singleton.INSTANCE.scheduler.schedule(runnable,cronTrigger);
        return storeFuture(future);
    }

    /**
     * 根据cron表达式启动定时任务(当前时区)
     * @param runnable 任务
     * @param cron cron表达式
     * @return
     */
    public static String scheduleByCron(Runnable runnable, String cron){
        return scheduleByCron(runnable, cron,TimeZone.getDefault());
    }

    /**
     * 启动一个延时任务
     * @param runnable
     * @param time
     * @return
     */
    public static String schedule(Runnable runnable,Date time){
        String id= RandomStringUtils.randomAlphanumeric(32);
        ListenableFutureTask futureTask=new ListenableFutureTask(runnable,id);
        futureTask.addCallback(Singleton.INSTANCE.successCallback,Singleton.INSTANCE.failureCallback);
        ScheduledFuture future= Singleton.INSTANCE.scheduler.schedule(futureTask,time);
        ID_TO_FUTURE.put(id,future);
        return id;
    }

    /**
     * 取消一个任务
     * @return
     */
    public static boolean cancel(String id,boolean mayInterruptIfRunning){
        ScheduledFuture future= ID_TO_FUTURE.remove(id);
        if(future==null){
            return true;
        }else{
            return future.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * 获取任务future
     * @param id
     * @return
     */
    public static ScheduledFuture get(String id){
        return ID_TO_FUTURE.get(id);
    }

    /**
     * 获取所有任务id
     * @return
     */
    public static Set<String> all(){
        return ID_TO_FUTURE.keySet();
    }

    private enum Singleton{
        INSTANCE;
        public ThreadPoolTaskScheduler scheduler;
        public SuccessCallback successCallback;
        public FailureCallback failureCallback;

        Singleton(){
            scheduler=new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
            scheduler.afterPropertiesSet();
            successCallback=id->{
                ID_TO_FUTURE.remove(id);
            };
            failureCallback=id->{
                ID_TO_FUTURE.remove(id);
            };
        }
    }
}
