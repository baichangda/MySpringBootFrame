package com.bcd.base.support_baidu;

import com.baidu.aip.imageclassify.AipImageClassify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BaiduConfig {
    @Value("${baidu.apiKey}")
    String apiKey;
    @Value("${baidu.secretKey}")
    String secretKey;
    @Bean
    public AipImageClassify aipImageClassify(){
        return new AipImageClassify("wx-bcd-aipImageClassify",apiKey,secretKey);
    }
}
