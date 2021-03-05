package com.bcd.config.swagger;

import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.assertj.core.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class Knife4jConfig {

    private RequestParameter requestParameter(){

        return new RequestParameterBuilder()
                .required(false)
                .name(ShiroHttpSession.DEFAULT_SESSION_ID_NAME)
                .description("shiro session id")
                .in(ParameterType.HEADER)
                .build();
    }

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .globalRequestParameters(Collections.singletonList(requestParameter()))
                .apiInfo(new ApiInfoBuilder()
                        .title("框架文档")
                        .description("框架文档")
                        .contact(
                                new Contact("bcd","13720278557","471267877@qq.com")
                        )
                        .version("1.0")
                        .build())
                //分组名称
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.bcd"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}