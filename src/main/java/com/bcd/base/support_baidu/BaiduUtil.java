package com.bcd.base.support_baidu;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.baidu.aip.ocr.AipOcr;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.dbinfo.mysql.util.DBInfoUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.html.HtmlEscapers;
import okhttp3.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
public class BaiduConfig {

    static Logger logger = LoggerFactory.getLogger(BaiduConfig.class);

    @Value("${baidu.apiKey}")
    String apiKey;
    @Value("${baidu.secretKey}")
    String secretKey;

    static String accessToken;
    static long expiredInSecond;

    static BaiduInterface baiduInterface = retrofit().create(BaiduInterface.class);

    @Bean
    public AipImageClassify aipImageClassify() {
        return new AipImageClassify("wx-bcd-aipImageClassify", apiKey, secretKey);
    }

    public static String getAccessToken() {
        if (accessToken == null) {
            synchronized (BaiduConfig.class) {
                if (accessToken == null) {
                    try {
                        final JsonNode jsonNode = baiduInterface.token("HXDSSTZbdtEgOnbo94jKfGDH", "6fzE3GL7G9I3hjzxnX6QYDSyNaopjvkf")
                                .execute().body();
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

    public static Retrofit retrofit() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(chain -> {
            final Request request = chain.request();
            final HttpUrl url = request.url();
            Request newRequest=request;
            if (!"/oauth/2.0/token".equals(url.encodedPath())) {
                newRequest=request.newBuilder().url(url.newBuilder().addQueryParameter("access_token", getAccessToken()).build()).build();
            }
            System.out.println(request.url());
            System.out.println(newRequest.url());
            return chain.proceed(newRequest);
        }).build();
        return new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com")
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
                .client(okHttpClient)
                .build();
    }

    public static String getToken(String apiKey, String secretKey) throws IOException {
        final OkHttpClient okHttpClient = new OkHttpClient();

        final Request req = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials" +
                        "&client_id=" + apiKey +
                        "&client_secret=" + secretKey)
                .build();
        final Response rep = okHttpClient.newCall(req).execute();
        final String res = rep.body().string();
        final JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(res);
        return jsonNode.get("access_token").asText();
    }

    public static String baiduFanyi(String str, String from, String to, String token) {
        try {
            final OkHttpClient okHttpClient = new OkHttpClient();
            Map<String, String> map = new HashMap<>();
            map.put("from", from);
            map.put("to", to);
            map.put("q", str);
            final RequestBody requestBody = RequestBody.create(MediaType.get("application/json;charset=utf-8"), JsonUtil.toJson(map));

            final Request req = new Request.Builder()
                    .header("Content-Type", "application/json;charset=utf-8")
                    .url("https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=" + token)
                    .post(requestBody).build();
            final Response rep = okHttpClient.newCall(req).execute();
            final String res = rep.body().string();
            final JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(res);
            final JsonNode arr = jsonNode.get("result").get("trans_result");
            StringBuilder sb = new StringBuilder();
            for (JsonNode node : arr) {
                sb.append(HtmlEscapers.htmlEscaper().escape(node.get("dst").asText()));
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    public static void test(String pdfDirPath) throws IOException {
        final JsonNode[] props = DBInfoUtil.getSpringProps("baidu.apiKey", "baidu.secretKey");
        AipOcr aipOcr = new AipOcr("wx-bcd-aipOcr", props[0].asText(), props[1].asText());
        final List<Path> filePathList = Files.list(Paths.get(pdfDirPath)).filter(e -> e.getFileName().toString().endsWith(".pdf")).toList();
        for (Path pdfPath : filePathList) {
            final String fileName = pdfPath.getFileName().toString();
            String pngDirPath = pdfDirPath + "/temp";
            Files.createDirectories(Paths.get(pngDirPath));
            String resPath = pdfDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
            Files.deleteIfExists(Paths.get(resPath));
            Files.createFile(Paths.get(resPath));

            PDDocument doc = PDDocument.load(pdfPath.toFile());
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageSize = doc.getNumberOfPages();
            try (final BufferedWriter bw = Files.newBufferedWriter(Paths.get(resPath))) {
                for (int i = 0; i < pageSize; i++) {
                    logger.info("handle pdf[{}] total[{}] pageNum[{}]", pdfPath, pageSize, i + 1);
                    //先提取png
                    Path pngPath = Paths.get(pngDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-" + (i + 1) + ".png");
                    Files.deleteIfExists(pngPath);
                    try (final OutputStream os = Files.newOutputStream(pngPath)) {
                        ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", os);
                    }
                    //调用百度识别
                    JSONObject jsonObject = null;
                    try {
                        HashMap<String, String> options = new HashMap<>();
                        options.put("language_type", "CHN_ENG");
                        jsonObject = aipOcr.basicAccurateGeneral(pngPath.toString(), options);
                        for (Object words_result : jsonObject.getJSONArray("words_result")) {
                            bw.write(((JSONObject) words_result).get("words").toString());
                            bw.newLine();
                        }
                        bw.newLine();
                        bw.write("================" + (i + 1) + "=================");
                        bw.newLine();
                        bw.newLine();
                        bw.flush();
                        jsonObject = null;
                    } catch (Exception ex) {
                        logger.info("handle pdf[{}] total[{}] pageNum[{}] failed", pdfPath, pageSize, i + 1, ex);
                        if (jsonObject != null) {
                            logger.info("baidu res:\n{}", jsonObject);
                        }
                    }
                }
            }
            doc.close();

        }
    }

    public static void test1(String pdfDirPath, String token) throws IOException, NoSuchFieldException {
        final JsonNode[] props = DBInfoUtil.getSpringProps("baidu.apiKey", "baidu.secretKey");
        AipOcr aipOcr = new AipOcr("wx-bcd-aipOcr", props[0].asText(), props[1].asText());
        final List<Path> filePathList = Files.list(Paths.get(pdfDirPath)).filter(e -> e.getFileName().toString().endsWith(".pdf")).toList();
        for (Path pdfPath : filePathList) {
            final String fileName = pdfPath.getFileName().toString();
            String pngDirPath = pdfDirPath + "/temp";
            Files.createDirectories(Paths.get(pngDirPath));
            String resPath1 = pdfDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-zh.txt";
            String resPath2 = pdfDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-en.txt";
            Files.deleteIfExists(Paths.get(resPath1));
            Files.deleteIfExists(Paths.get(resPath2));
            Files.createFile(Paths.get(resPath1));
            Files.createFile(Paths.get(resPath2));

            PDDocument doc = PDDocument.load(pdfPath.toFile());
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageSize = doc.getNumberOfPages();
            try (final BufferedWriter bw1 = Files.newBufferedWriter(Paths.get(resPath1));
                 final BufferedWriter bw2 = Files.newBufferedWriter(Paths.get(resPath2))) {
                for (int i = 0; i < pageSize; i++) {
                    logger.info("handle pdf[{}] total[{}] pageNum[{}]", pdfPath, pageSize, i + 1);
                    //先提取png
                    Path pngPath = Paths.get(pngDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-" + (i + 1) + ".png");
                    Files.deleteIfExists(pngPath);
                    try (final OutputStream os = Files.newOutputStream(pngPath)) {
                        ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", os);
                    }
                    //调用百度识别
                    JSONObject jsonObject = null;
                    try {
                        HashMap<String, String> options = new HashMap<>();
                        options.put("language_type", "CHN_ENG");
                        jsonObject = aipOcr.basicAccurateGeneral(pngPath.toString(), options);
                        StringBuilder sb = new StringBuilder();
                        for (Object words_result : jsonObject.getJSONArray("words_result")) {
                            sb.append(((JSONObject) words_result).get("words").toString());
                            sb.append("\n");
                        }
                        final String fanyiRes = baiduFanyi(sb.toString(), "zh", "en", token);
                        bw1.write(sb.toString());
                        bw2.write(fanyiRes);
                        bw1.newLine();
                        bw1.write("================" + (i + 1) + "=================");
                        bw1.newLine();
                        bw1.newLine();
                        bw1.flush();

                        bw2.newLine();
                        bw2.write("================" + (i + 1) + "=================");
                        bw2.newLine();
                        bw2.newLine();
                        bw2.flush();
                        jsonObject = null;
                    } catch (Exception ex) {
                        logger.info("handle pdf[{}] total[{}] pageNum[{}] failed", pdfPath, pageSize, i + 1, ex);
                        if (jsonObject != null) {
                            logger.info("baidu res:\n{}", jsonObject);
                        }
                    }
                }
            }
            doc.close();

        }
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException {
//        final String token = getToken("HXDSSTZbdtEgOnbo94jKfGDH", "6fzE3GL7G9I3hjzxnX6QYDSyNaopjvkf");
//        final String s = baiduFanyi("哈哈哈", "zh", "en", token);
//        test1("/Users/baichangda/pdftemp", token);
//        test1("/Users/baichangda/pdftest", token);
//        test("/Users/baichangda/pdftest");

        BaiduConfig baiduConfig = new BaiduConfig();
        final Retrofit retrofit = baiduConfig.retrofit();
        final BaiduInterface baiduInterface = retrofit.create(BaiduInterface.class);
        final JsonNode jsonNode = baiduInterface.token("HXDSSTZbdtEgOnbo94jKfGDH", "6fzE3GL7G9I3hjzxnX6QYDSyNaopjvkf").execute().body();
        System.out.println(jsonNode);

    }
}
