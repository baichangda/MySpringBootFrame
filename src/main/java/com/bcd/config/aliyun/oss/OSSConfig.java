package com.bcd.config.aliyun.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@EnableConfigurationProperties(AliyunProperties.class)
public class OSSConfig {
    @Autowired
    AliyunProperties aliyunProperties;
    @Bean
    public OSS oss(){
        OSS oss= new OSSClientBuilder().build(aliyunProperties.oss.endPoint,aliyunProperties.accessKeyId, aliyunProperties.accessKeySecret);
        return oss;
    }
}
