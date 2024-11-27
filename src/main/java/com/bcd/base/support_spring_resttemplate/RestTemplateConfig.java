package com.bcd.base.support_spring_resttemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 配置spring的restTemplate , 此对象可以用来发送 restful 请求
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    /**
     * 使用spring设置的消息转换器
     *
     * @return
     */
    @Bean("restTemplate")
    public RestTemplate restTemplate() {
        return restTemplateBuilder.connectTimeout(Duration.ofSeconds(10)).readTimeout(Duration.ofSeconds(30))
                .build();
    }
}
