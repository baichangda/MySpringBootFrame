package com.bcd.config.redis.schedule.demo;

import com.bcd.config.redis.schedule.handler.RedisScheduleClusterHandler;
import com.bcd.config.redis.schedule.handler.impl.ClusterFailedScheduleHandler;
import com.bcd.config.redis.schedule.handler.impl.SingleFailedScheduleHandler;
import com.bcd.sys.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Configurable
@EnableScheduling
public class TestSchedule {

    @Autowired
    private LogService logService;

    private String lockId="bcd-test";

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void test1(){
        RedisScheduleClusterHandler handler=new ClusterFailedScheduleHandler(lockId,2000L);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println(Thread.currentThread().getId()+" exception===========test1 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test1 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void test2(){
        RedisScheduleClusterHandler handler=new ClusterFailedScheduleHandler(lockId,2000L);
        boolean res= handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println(Thread.currentThread().getId()+" exception===========test2 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test2 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void test3(){
        RedisScheduleClusterHandler handler=new ClusterFailedScheduleHandler(lockId,2000L);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println(Thread.currentThread().getId()+" exception===========test3 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test3 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void test4(){
        RedisScheduleClusterHandler handler=new ClusterFailedScheduleHandler(lockId,2000L);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            System.out.println(Thread.currentThread().getId()+"===========test4 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }
}
