package com.bcd.base.support_jackson;

import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return JsonUtil.GLOBAL_OBJECT_MAPPER;
    }

}
