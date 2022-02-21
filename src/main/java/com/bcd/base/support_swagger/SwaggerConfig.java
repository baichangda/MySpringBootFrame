package com.bcd.base.support_swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${sa-token.token-name}")
    String tokenName;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("API")
                        .description("bcd application")
                        .version("v0.0.1"));
    }

    /**
     * 添加全局的请求头参数
     */
//    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> {
                    operation.addParametersItem(new HeaderParameter().name(tokenName).description("session id"));
                });
    }
}
