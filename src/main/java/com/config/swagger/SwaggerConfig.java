package com.config.swagger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by Administrator on 2017/4/10.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @SuppressWarnings("unchecked")
    @Bean
    public Docket testApi(){
        Docket docket = new Docket(DocumentationType.SPRING_WEB)
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
                .pathMapping("/")// base，最终调用接口后会和paths拼接在一起
                .select()
                .build();
        return docket;
    }
}
