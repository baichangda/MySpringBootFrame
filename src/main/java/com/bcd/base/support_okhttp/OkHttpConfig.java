package com.bcd.base.support_okhttp;

import com.bcd.base.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetrofitConfig {

    @Bean
    public Retrofit localRetrofit() {
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
                .baseUrl("http://127.0.0.1:" + port)
                .build();
    }

}
