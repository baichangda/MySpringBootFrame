package com.bcd.config.retrofit;

import com.bcd.base.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Value("${server.port:8888}")
    int port;

    @Bean
    public Retrofit localRetrofit() {
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
                .baseUrl("http://127.0.0.1:" + port)
                .build();
    }

}
