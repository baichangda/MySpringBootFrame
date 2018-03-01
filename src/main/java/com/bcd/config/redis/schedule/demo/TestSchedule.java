package com.bcd.config.redis.schedule.demo;

import com.bcd.config.redis.schedule.RedisScheduleClusterHandler;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Configurable
@EnableScheduling
public class TestSchedule {

    private String lockId="bcd-test";

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "*/3 * * * * ?")
    public void test1(){
        RedisScheduleClusterHandler handler=new RedisScheduleClusterHandler(lockId,2000L,2);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println("exception===========test1 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test1 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void test2(){
        RedisScheduleClusterHandler handler=new RedisScheduleClusterHandler(lockId,2000L,2);
        boolean res= handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println("exception===========test2 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test2 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void test3(){
        RedisScheduleClusterHandler handler=new RedisScheduleClusterHandler(lockId,2000L,2);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            String a=null;
            System.out.println("exception===========test3 "+sdf.format(new Date()));
            a.toString();
            System.out.println("===========test3 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void test4(){
        RedisScheduleClusterHandler handler=new RedisScheduleClusterHandler(lockId,2000L,2);
        boolean res=handler.doBeforeStart();
        if(!res){
            return;
        }
        try {
            System.out.println("===========test4 "+sdf.format(new Date()));
            handler.doOnSuccess();
        }catch (Exception e){
            handler.doOnFailed();
        }
    }
}
