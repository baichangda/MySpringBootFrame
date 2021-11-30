package com.bcd.base.support_baidu;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;


public interface BaiduInterface {
    /**
     * 获取access_token
     * https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjhhu
     */
    @POST("/oauth/2.0/token?grant_type=client_credentials")
    Call<JsonNode> token(
            @Query("client_id") String client_id,
            @Query("client_secret") String client_secret);


    /**
     * 翻译
     * https://ai.baidu.com/ai-doc/MT/4kqryjku9
     */
    @Headers({
            "Content-Type:application/json;charset=utf-8"
    })
    @POST("/rpc/2.0/mt/texttrans/v1")
    Call<JsonNode> translation(@Body Map<String, String> param);


    /**
     * ocr标准版
     * https://ai.baidu.com/ai-doc/OCR/zk3h7xz52
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/ocr/v1/general_basic")
    @FormUrlEncoded
    Call<JsonNode> ocrGeneral(@Field("image") String image,
                              @Field("url") String url,
                              @Field("pdf_file") String pdf_file,
                              @Field("pdf_file_num") String pdf_file_num,
                              @Field("language_type") String language_type,
                              @Field("detect_direction") String detect_direction,
                              @Field("detect_language") String detect_language,
                              @Field("paragraph") String paragraph,
                              @Field("probability") String probability);

    /**
     * ocr高精度版
     * https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/ocr/v1/accurate_basic")
    @FormUrlEncoded
    Call<JsonNode> ocrAccurate(@Field("image") String image,
                               @Field("url") String url,
                               @Field("pdf_file") String pdf_file,
                               @Field("pdf_file_num") String pdf_file_num,
                               @Field("language_type") String language_type,
                               @Field("detect_direction") String detect_direction,
                               @Field("paragraph") String paragraph,
                               @Field("probability") String probability);


    /**
     * 办公文档图片识别
     * https://ai.baidu.com/ai-doc/OCR/ykg9c09ji
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/ocr/v1/doc_analysis_office")
    @FormUrlEncoded
    Call<JsonNode> ocrDoc(@Field("image") String image,
                          @Field("url") String url,
                          @Field("pdf_file") String pdf_file,
                          @Field("pdf_file_num") String pdf_file_num,
                          @Field("language_type") String language_type,
                          @Field("result_type") String result_type,
                          @Field("detect_direction") String detect_direction,
                          @Field("line_probability") String line_probability,
                          @Field("words_type") String words_type,
                          @Field("layout_analysis") String layout_analysis,
                          @Field("erase_seal") String erase_seal);

    /**
     * 车型识别
     * https://ai.baidu.com/ai-doc/OCR/ykg9c09ji
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/image-classify/v1/car")
    @FormUrlEncoded
    Call<JsonNode> carType(@Field("image") String image,
                           @Field("url") String url,
                           @Field("top_num") String top_num,
                           @Field("baike_num") String baike_num);


    /**
     * 车辆损伤识别
     * https://ai.baidu.com/ai-doc/VEHICLE/fk3hb3f5w
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/image-classify/v1/vehicle_damage")
    @FormUrlEncoded
    Call<JsonNode> vehicleDamage(@Field("image") String image,
                                 @Field("url") String url);

    /**
     * 人像动漫化
     * https://cloud.baidu.com/doc/IMAGEPROCESS/s/Mk4i6olx5
     */
    @Headers({
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("/rest/2.0/image-process/v1/selfie_anime")
    @FormUrlEncoded
    Call<JsonNode> selfieAnime(@Field("image") String image,
                               @Field("url") String url,
                               @Field("type") String type,
                               @Field("mask_id") String mask_id);


}
