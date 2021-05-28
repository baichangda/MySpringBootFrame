package com.bcd.base.support_baidu;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;

public class BaiduUtil {
    static Logger logger = LoggerFactory.getLogger(BaiduUtil.class);

    static String client_id = "oyWrN0Vc7OtoZrPtGu6ub20H";
    static String client_secret = "058PQmO16nVPpuydtvKheZUluGLZ7Av6";
    static String access_token = null;
    static long accessTokenExpireInSecond = -1L;

    public static String getAccessToken() throws IOException {
        if (accessTokenExpireInSecond == -1L || Instant.now().getEpochSecond() >= accessTokenExpireInSecond) {
            synchronized (BaiduUtil.class) {
                if (accessTokenExpireInSecond == -1L || Instant.now().getEpochSecond() >= accessTokenExpireInSecond) {
                    Response<JsonNode> response = BaiduHttp.instance.token(client_id, client_secret).execute();
                    logger.info("call getAccessToken:\n{}", response.body().toPrettyString());
                    JsonNode jsonNode = response.body();
                    accessTokenExpireInSecond = Instant.now().getEpochSecond() + jsonNode.get("expires_in").asLong();
                    access_token = response.body().get("access_token").asText();
                }
            }
        }
        return access_token;
    }

    public static void main(String[] args) throws IOException {
        getAccessToken();
    }
}
