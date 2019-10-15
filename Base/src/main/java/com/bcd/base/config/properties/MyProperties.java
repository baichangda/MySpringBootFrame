package com.bcd.base.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "my")
public class MyProperties {
    //是否启用集群定时任务失败注解
    boolean enableScheduleFailedAnnotation;
}
