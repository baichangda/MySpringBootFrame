package com.bcd.config.resttemplate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
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
     * @param mappingJackson2HttpMessageConverter
     * @return
     */
    @Bean("restTemplate")
    public RestTemplate restTemplate(ClientHttpRequestFactory factory,
                                     @Qualifier("mappingJackson2HttpMessageConverter_my") MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        RestTemplate restTemplate = new RestTemplate(factory);
        //在此添加转换器配置,移除默认
        restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().removeIf(e->e.getClass().isAssignableFrom(MappingJackson2HttpMessageConverter.class));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);// ms
        factory.setReadTimeout(30000);// ms
        return factory;
    }
}
