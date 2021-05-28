package com.bcd.base.support_baidu;

import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.*;

public interface BaiduHttp {
    BaiduHttp instance=new Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
            .baseUrl("https://aip.baidubce.com")
            .build()
            .create(BaiduHttp.class);

    @POST("/oauth/2.0/token?grant_type=client_credentials")
    Call<JsonNode> token(@Query("client_id") String client_id,
                         @Query("client_secret") String client_secret);

    @FormUrlEncoded
    @POST("/rest/2.0/ocr/v1/accurate_basic")
    Call<JsonNode> orc(@Query("access_token") String access_token,
                       @Field("image") String image,
                       @Field("url") String url,
                       @Field("language_type") String language_type);
}
