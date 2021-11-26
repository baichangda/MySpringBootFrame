package com.bcd.base.support_baidu;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BaiduInstance {
    Logger logger = LoggerFactory.getLogger(BaiduInstance.class);

    private final String clientId;
    private final String clientSecret;
    private final BaiduInterface baiduInterface;

    private volatile String accessToken;
    private volatile long expiredInSecond;

    private BaiduInstance(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baiduInterface = newRetrofit().create(BaiduInterface.class);
    }


    public static BaiduInstance newInstance(String clientId, String clientSecret) {
        return new BaiduInstance(clientId, clientSecret);
    }

    public BaiduInterface getBaiduInterface() {
        return baiduInterface;
    }

    private String getAccessToken() {
        if (accessToken == null || expiredInSecond < Instant.now().getEpochSecond()) {
            synchronized (this) {
                if (accessToken == null || expiredInSecond < Instant.now().getEpochSecond()) {
                    try {
                        final JsonNode jsonNode =  baiduInterface.token(clientId, clientSecret).execute().body();
                        logger.info("access_token:\n{}", jsonNode.toPrettyString());
                        accessToken = jsonNode.get("access_token").asText();
                        expiredInSecond = Instant.now().getEpochSecond() + jsonNode.get("expires_in").asLong() - 60;
                    } catch (IOException ex) {
                        throw BaseRuntimeException.getException(ex);
                    }
                }
            }
        }
        return accessToken;
    }

    private Retrofit newRetrofit() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    final Request request = chain.request();
                    final HttpUrl url = request.url();
                    Request newRequest = request;
                    if (!"/oauth/2.0/token".equals(url.encodedPath())) {
                        newRequest = request.newBuilder().url(url.newBuilder().addQueryParameter("access_token", getAccessToken()).build()).build();
                    }
//                                logger.info("{}", request.url());
//                                logger.info("{}", newRequest.url());
                    return chain.proceed(newRequest);
                })
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectionPool(new ConnectionPool(2,60, TimeUnit.SECONDS))
                .build();
        return new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com")
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
                .client(okHttpClient)
                .build();
    }


    public JsonNode translation(String str, String from, String to) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("from", from);
        map.put("to", to);
        map.put("q", str);
        return baiduInterface.translation(map).execute().body();
    }

    public JsonNode ocr_imagePath(String imagePath, String languageType) throws IOException {
        return baiduInterface.ocr(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(imagePath))), null, null, null, languageType, null, null, null).execute().body();
    }

    public JsonNode ocr_imageBase64(String imageBase64, String languageType) throws IOException {
        return baiduInterface.ocr(imageBase64, null, null, null, languageType, null, null, null).execute().body();
    }

    public JsonNode ocr_imageUrl(String imageUrl, String languageType) throws IOException {
        return baiduInterface.ocr(null, imageUrl, null, null, languageType, null, null, null).execute().body();
    }

    public JsonNode ocr_pdf(String pdfFile, int pdfFileNum, String languageType) throws IOException {
        return baiduInterface.ocr(null, null, pdfFile, pdfFileNum + "", languageType, null, null, null).execute().body();
    }

    public JsonNode ocrDoc_imagePath(String imagePath, String languageType) throws IOException {
        return baiduInterface.ocrDoc(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(imagePath))), null, null, null, languageType, null, null, null, null, null, null).execute().body();
    }

    public JsonNode carType_imageUrl(String imageUrl, int topNum) throws IOException {
        return baiduInterface.carType(imageUrl, topNum + "", null).execute().body();
    }

    public JsonNode carType_imageBase64(String imageBase64, int topNum) throws IOException {
        return baiduInterface.carType(imageBase64, topNum + "", null).execute().body();
    }

    public JsonNode carType_imagePath(String imagePath, int topNum) throws IOException {
        return baiduInterface.carType(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(imagePath))), topNum + "", null).execute().body();
    }

}
