package com.bcd.config.resttemplate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置spring的restTemplate , 此对象可以用来发送 restful 请求
 */
@Configuration
public class RestTemplateConfig {
    /**
     * 使用spring设置的消息转换器
     * @param factory
     * @param httpMessageConverter
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(ClientHttpRequestFactory factory,@Qualifier("httpMessageConverter") HttpMessageConverter httpMessageConverter) {
        RestTemplate restTemplate = new RestTemplate(factory);
        List<HttpMessageConverter<?>> messageConverters=new ArrayList<>();
        //在此添加转换器配置
        messageConverters.add(httpMessageConverter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);// ms
        factory.setReadTimeout(10000);// ms
        return factory;
    }
}
