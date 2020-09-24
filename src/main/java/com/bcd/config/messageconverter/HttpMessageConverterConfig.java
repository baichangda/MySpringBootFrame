package com.bcd.config.messageconverter;

import com.bcd.base.util.JsonUtil;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

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
}
