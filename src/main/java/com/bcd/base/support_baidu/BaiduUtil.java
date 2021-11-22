package com.bcd.base.support_baidu;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaiduUtil {

    static Logger logger = LoggerFactory.getLogger(BaiduUtil.class);

//    private final static String clientId = "HXDSSTZbdtEgOnbo94jKfGDH";
//    private final static String clientSecret = "6fzE3GL7G9I3hjzxnX6QYDSyNaopjvkf";

    private final static String clientId = "5GjWi9nxIXvZbqVujPIj8xCl";
    private final static String clientSecret = "v90dNdiApdNXYeLvI5zohSfEZ6EbGvwo";

    private static String accessToken;
    private static long expiredInSecond;

    private static Retrofit retrofit;
    private static BaiduInterface baiduInterface;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (BaiduUtil.class) {
                if (retrofit == null) {
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
                            .build();
                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://aip.baidubce.com")
                            .addConverterFactory(JacksonConverterFactory.create(JsonUtil.GLOBAL_OBJECT_MAPPER))
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static BaiduInterface getBaiduInterface() {
        if (baiduInterface == null) {
            synchronized (BaiduUtil.class) {
                if (baiduInterface == null) {
                    baiduInterface = getRetrofit().create(BaiduInterface.class);
                }
            }
        }
        return baiduInterface;
    }

    /**
     * https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjhhu
     *
     * @return
     */
    public static String getAccessToken() {
        if (accessToken == null || expiredInSecond < Instant.now().getEpochSecond()) {
            synchronized (BaiduUtil.class) {
                if (accessToken == null || expiredInSecond < Instant.now().getEpochSecond()) {
                    try {
                        final JsonNode jsonNode = baiduInterface.token(clientId, clientSecret)
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


    /**
     * https://ai.baidu.com/ai-doc/MT/4kqryjku9
     *
     * @param str
     * @param from
     * @param to
     * @return
     */
    public static JsonNode translation(String str, String from, String to) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("from", from);
            map.put("to", to);
            map.put("q", str);
            return getBaiduInterface().translation(map).execute().body();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 识别图片文字(base64格式)
     * https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
     *
     * @param imagePath
     * @param languageType
     * @return
     */
    public static JsonNode ocr_imagePath(String imagePath, String languageType) {
        try {
            final byte[] bytes = Files.readAllBytes(Paths.get(imagePath));
            return getBaiduInterface().ocr(Base64.getEncoder().encodeToString(bytes), null, null, null, languageType, null, null, null).execute().body();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 识别图片文字(base64格式)
     * https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
     *
     * @param imageBase64
     * @param languageType
     * @return
     */
    public static JsonNode ocr_imageBase64(String imageBase64, String languageType) {
        try {
            return getBaiduInterface().ocr(imageBase64, null, null, null, languageType, null, null, null).execute().body();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 识别指定url图片文字
     * https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
     *
     * @param url
     * @param languageType
     * @return
     */
    public static JsonNode ocr_url(String url, String languageType) {
        try {
            return getBaiduInterface().ocr(null, url, null, null, languageType, null, null, null).execute().body();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 识别pdf指定页文字
     * https://ai.baidu.com/ai-doc/OCR/1k3h7y3db
     *
     * @param pdfFile
     * @param pdfFileNum
     * @param languageType
     * @return
     */
    public static JsonNode ocr_pdf(String pdfFile, int pdfFileNum, String languageType) {
        try {
            return getBaiduInterface().ocr(null, null, pdfFile, pdfFileNum + "", languageType, null, null, null).execute().body();
        } catch (IOException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    /**
     * 识别所有pdf下面的文字
     *
     * @param pdfDirPath
     * @param languageType
     * @throws IOException
     */
    public static void allPdfOcr(String pdfDirPath, String languageType) throws IOException {
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
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = ocr_imagePath(pngPath.toString(), languageType);
                        for (JsonNode words_result : jsonNode.get("words_result")) {
                            bw.write(words_result.get("words").asText());
                            bw.newLine();
                        }
                        bw.newLine();
                        bw.write("================" + (i + 1) + "=================");
                        bw.newLine();
                        bw.newLine();
                        bw.flush();
                    } catch (Exception ex) {
                        logger.info("handle pdf[{}] total[{}] pageNum[{}] failed", pdfPath, pageSize, i + 1, ex);
                        if (jsonNode != null) {
                            logger.info("baidu res:\n{}", jsonNode);
                        }
                    }
                }
            }
            doc.close();

        }
    }

    /**
     * 识别所有pdf下面的文字并翻译
     *
     * @param pdfDirPath
     * @param languageType
     * @param from
     * @param to 
     * @throws IOException
     */
    public static void allPdfOcrAndTranslation(String pdfDirPath, String languageType, String from, String to) throws IOException {
        final List<Path> filePathList = Files.list(Paths.get(pdfDirPath)).filter(e -> e.getFileName().toString().endsWith(".pdf")).toList();
        for (Path pdfPath : filePathList) {
            final String fileName = pdfPath.getFileName().toString();
            String pngDirPath = pdfDirPath + "/temp";
            Files.createDirectories(Paths.get(pngDirPath));
            String resPath1 = pdfDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-from.txt";
            String resPath2 = pdfDirPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "-to.txt";
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
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = ocr_imagePath(pngPath.toString(), languageType);
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode words_result : jsonNode.get("words_result")) {
                            sb.append(words_result.get("words").asText());
                            sb.append("\n");
                        }
                        final String fanyiRes = translation(sb.toString(), from, to).get("trans_result").get(0).get("dst").asText();
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
                        jsonNode = null;
                    } catch (Exception ex) {
                        logger.info("handle pdf[{}] total[{}] pageNum[{}] failed", pdfPath, pageSize, i + 1, ex);
                        if (jsonNode != null) {
                            logger.info("baidu res:\n{}", jsonNode);
                        }
                    }
                }
            }
            doc.close();

        }
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException {
        allPdfOcr("/Users/baichangda/pdftest", "CHN_ENG");
        allPdfOcrAndTranslation("/Users/baichangda/pdftest", "CHN_ENG", "zh", "en");
        System.out.println(translation("啊啊啊", "zh", "en"));

    }
}
