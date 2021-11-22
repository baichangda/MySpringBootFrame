package com.bcd.base.support_baidu;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;


public interface BaiduInterface {
    @POST("/oauth/2.0/token?grant_type=client_credentials")
    Call<JsonNode> token(
            @Query("client_id") String client_id,
            @Query("client_secret") String client_secret);


    @Headers({
            "Content-Type:application/json;charset=utf-8"
    })
    @POST("/rpc/2.0/mt/texttrans/v1")
    Call<JsonNode> translation(@Body Map<String, String> param);


    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/ocr/v1/accurate_basic")
    @FormUrlEncoded
    Call<JsonNode> ocr(@Field("image") String image,
                       @Field("url") String url,
                       @Field("pdf_file") String pdf_file,
                       @Field("pdf_file_num") String pdf_file_num,
                       @Field("language_type") String language_type,
                       @Field("detect_direction") String detect_direction,
                       @Field("paragraph") String paragraph,
                       @Field("probability") String probability);
}
