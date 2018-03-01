package com.bcd.config.redis.schedule;

import org.springframework.scheduling.annotation.Scheduled;


public class TestSchedule {

    @Scheduled(cron = "*/10 * * * * ?")
    public void test1(){
        ScheduleUtil.doBeforeStart("test",3000L);
        try {
            System.out.println("===========test1");
            ScheduleUtil.doOnSuccess("test",3000L);
        }catch (Exception e){
            ScheduleUtil.doOnFailed("test");
        }
    }

    @Scheduled(cron = "*/10 * * * * ?")
    public void test2(){
        ScheduleUtil.doBeforeStart("test",3000L);
        try {
            System.out.println("===========test2");
            ScheduleUtil.doOnSuccess("test",3000L);
        }catch (Exception e){
            ScheduleUtil.doOnFailed("test");
        }
    }

    @Scheduled(cron = "*/10 * * * * ?")
    public void test3(){
        ScheduleUtil.doBeforeStart("test",3000L);
        try {
            System.out.println("===========test3");
            ScheduleUtil.doOnSuccess("test",3000L);
        }catch (Exception e){
            ScheduleUtil.doOnFailed("test");
        }
    }

    @Scheduled(cron = "*/10 * * * * ?")
    public void test4(){
        ScheduleUtil.doBeforeStart("test",3000L);
        try {
            System.out.println("===========test4");
            ScheduleUtil.doOnSuccess("test",3000L);
        }catch (Exception e){
            ScheduleUtil.doOnFailed("test");
        }
    }
}
