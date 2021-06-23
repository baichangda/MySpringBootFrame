package com.bcd.base.support_baidu;

import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.io.IOException;
import java.time.Instant;

public interface BaiduHttp {

    String client_id = "Klz8YKRBRGrLt1AqZkvbrFV9";
    String client_secret = "ltoBm51TfiidKDYMTljHajMoXuqcC6E8";
    Object[] access_data = new Object[2];
    BaiduHttp instance = new Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
            .baseUrl("https://aip.baidubce.com")
            .client(new OkHttpClient.Builder().addInterceptor(chain -> {
                Request request = chain.request();
                HttpUrl httpUrl = request.url();
                if (httpUrl.url().getPath().contains("/oauth/2.0/token")) {
                    return chain.proceed(request);
                } else {
                    HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
                    httpUrlBuilder.addQueryParameter("access_token", getAccessToken());
                    Request newRequest = request.newBuilder().url(httpUrlBuilder.build()).build();
                    return chain.proceed(newRequest);
                }
            }).build())
            .build()
            .create(BaiduHttp.class);

    static String getAccessToken() throws IOException {
        if (access_data[1] == null || Instant.now().getEpochSecond() >= (long) access_data[1]) {
            synchronized (BaiduHttp.class) {
                if (access_data[1] == null || Instant.now().getEpochSecond() >= (long) access_data[1]) {
                    Response<JsonNode> response = instance.token(client_id, client_secret).execute();
                    JsonNode jsonNode = response.body();
                    access_data[0] = jsonNode.get("access_token").asText();
                    access_data[1] = Instant.now().getEpochSecond() + jsonNode.get("expires_in").asLong() - 10;
                }
            }
        }
        return (String) access_data[0];
    }

    @POST("/oauth/2.0/token?grant_type=client_credentials")
    Call<JsonNode> token(@Query("client_id") String client_id,
                         @Query("client_secret") String client_secret);

    @FormUrlEncoded
    @POST("/rest/2.0/ocr/v1/accurate_basic")
    Call<JsonNode> orc(@Field("image") String image,
                       @Field("url") String url,
                       @Field("language_type") String language_type);

    @FormUrlEncoded
    @POST("/rest/2.0/image-classify/v1/car")
    Call<JsonNode> car(@Field("image") String image,
                       @Field("url") String url,
                       @Field("top_num") Integer top_num,
                       @Field("baike_num") Integer baike_num);


}
