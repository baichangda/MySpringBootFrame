package com.bcd.config.messageconverter;

import com.bcd.base.util.JsonUtil;
import com.bcd.base.util.XmlUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;



@Configuration
public class HttpMessageConverterConfig{
    @Bean(name = "mappingJackson2HttpMessageConverter")
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
        MappingJackson2HttpMessageConverter httpMessageConverter=new MappingJackson2HttpMessageConverter(JsonUtil.GLOBAL_OBJECT_MAPPER);
        return httpMessageConverter;
    }

    @Bean(name = "mappingJackson2XmlHttpMessageConverter")
    public MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter(){
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
        MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter=new MappingJackson2XmlHttpMessageConverter(XmlUtil.GLOBAL_XML_MAPPER);
        return xmlHttpMessageConverter;
    }
}
