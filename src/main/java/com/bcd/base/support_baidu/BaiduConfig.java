package com.bcd.base.support_baidu;

import com.baidu.aip.ocr.AipOcr;
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
    public BaiduInstance baiduInstance() {
        return BaiduInstance.newInstance(apiKey, secretKey);
    }

    @Bean
    public AipOcr aipOcr() {
        return new AipOcr("test",apiKey, secretKey);
    }
}
