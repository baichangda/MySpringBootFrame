package com.bcd.config.messageconverter;

import com.bcd.base.util.JsonUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class HttpMessageConverterConfig implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter= contextRefreshedEvent.getApplicationContext().getBean(RequestMappingHandlerAdapter.class);
        //移除默认的MappingJackson2HttpMessageConverter
        requestMappingHandlerAdapter.getMessageConverters().removeIf(e->e instanceof MappingJackson2HttpMessageConverter&& e!=httpMessageConverter());
    }

    @Bean(name = "httpMessageConverter")
    public MappingJackson2HttpMessageConverter httpMessageConverter(){
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
        MappingJackson2HttpMessageConverter httpMessageConverter=new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_PROBLEM_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_PROBLEM_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_STREAM_JSON);
        httpMessageConverter.setObjectMapper(JsonUtil.GLOBAL_OBJECT_MAPPER);
        httpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        return httpMessageConverter;
    }
}
