package com.bcd.config.messageconverter;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * 配置此类的原因是因为要用 FastJson 来完成 SpringMVC Controller 返回结果的解析(不使用spring自带的转换器)
 */
@Configuration
public class MessageConverterConfig {
    @Bean
    public HttpMessageConverter httpMessageConverter(){
        FastJsonConfig fastJsonConfig=new FastJsonConfig();
        FastJsonHttpMessageConverter converter= new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(fastJsonConfig);
        return converter;
    }
}
