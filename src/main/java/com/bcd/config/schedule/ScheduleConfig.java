package com.bcd.config.schedule;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class ScheduleConfig implements SchedulingConfigurer{
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //开启多线程执行定时任务,默认只有单线程执行
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }
}
