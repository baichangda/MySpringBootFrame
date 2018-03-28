package com.bcd.config.messageconverter;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置此类的原因是因为要用 FastJson 来完成 SpringMVC Controller 返回结果的解析(不使用spring自带的转换器)
 */
@Configuration
public class MessageConverterConfig {
    @Bean
    public HttpMessageConverter fastJsonHttpMessageConverter(){
        FastJsonConfig fastJsonConfig=new FastJsonConfig();
        FastJsonHttpMessageConverter converter= new FastJsonHttpMessageConverter();
        /**
         * 必须设置支持的数据类型(不能设置为*);否则可能会导致请求报错
         * java.lang.IllegalArgumentException: 'Content-Type' cannot contain wildcard type '*'
         * 因为在 org.springframework.http.converter.AbstractHttpMessageConverter.write 设置有保护机制;不允许设置*
         *
         * 注意:spring默认加载了
         * stringHttpMessageConverter HttpMessageConvertersAutoConfiguration$StringHttpMessageConverterConfiguration
         * mappingJackson2HttpMessageConverter JacksonHttpMessageConvertersConfiguration$MappingJackson2HttpMessageConverterConfiguration
         * 覆盖这两个类的方式就是通过设置 setSupportedMediaTypes 来替换
         */
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        converter.setFastJsonConfig(fastJsonConfig);
        return converter;
    }
}
