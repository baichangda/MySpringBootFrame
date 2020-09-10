package com.bcd.config.messageconverter;

import com.bcd.base.util.JsonUtil;
import com.bcd.base.util.XmlUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * 设置spring mvc的结果转换器
 *
 * 只需要注册 HttpMessageConverter 的bean、spring会自动扫描并加入到 HttpMessageConverters中
 *
 * 同时会进行顺序调整,针对同类型的HttpMessageConverter 放在对应的默认前面;否则放在优先级最高
 * 参考如下代码中的逻辑
 * {@link HttpMessageConverters#getCombinedConverters(Collection, List)}
 */
@Configuration
public class HttpMessageConverterConfig{
    @Bean(name = "mappingJackson2HttpMessageConverter_my")
    public MappingJackson2HttpMessageConverter httpMessageConverter(){
        MappingJackson2HttpMessageConverter httpMessageConverter=new MappingJackson2HttpMessageConverter(JsonUtil.GLOBAL_OBJECT_MAPPER);
        return httpMessageConverter;
    }

    @Bean(name = "mappingJackson2XmlHttpMessageConverter_my")
    public MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter(){
        MappingJackson2XmlHttpMessageConverter xmlHttpMessageConverter=new MappingJackson2XmlHttpMessageConverter(XmlUtil.GLOBAL_XML_MAPPER);
        return xmlHttpMessageConverter;
    }

}
