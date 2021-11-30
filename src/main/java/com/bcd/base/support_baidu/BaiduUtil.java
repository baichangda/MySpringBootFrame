package com.bcd.base.support_baidu;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;


public class BaiduUtil {

    static Logger logger = LoggerFactory.getLogger(BaiduUtil.class);

    private final static String clientId = "5GjWi9nxIXvZbqVujPIj8xCl";
    private final static String clientSecret = "v90dNdiApdNXYeLvI5zohSfEZ6EbGvwo";
    private final static BaiduInstance baiduInstance = BaiduInstance.newInstance(clientId, clientSecret);


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
                    final String imageBase64;
                    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", os);
                        imageBase64 = Base64.getEncoder().encodeToString(os.toByteArray());
                    }
                    //调用百度识别
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = baiduInstance.ocrGeneral_imageBase64(imageBase64, languageType);
                        for (JsonNode words_result : jsonNode.get("words_result")) {
                            bw.write(words_result.get("words").asText());
                            bw.newLine();
                        }
//                        for (JsonNode result : jsonNode.get("results")) {
//                            bw.write(result.get("words").get("word").asText());
//                            bw.newLine();
//                        }
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
                    final String imageBase64;
                    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", os);
                        imageBase64 = Base64.getEncoder().encodeToString(os.toByteArray());
                    }
                    //调用百度识别
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = baiduInstance.ocrGeneral_imageBase64(imageBase64, languageType);
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode words_result : jsonNode.get("words_result")) {
                            sb.append(words_result.get("words").asText());
                            sb.append("\n");
                        }
                        final String fanyiRes = baiduInstance.translation(sb.toString(), from, to).get("trans_result").get(0).get("dst").asText();
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
//        allPdfOcr("/Users/baichangda/pdftest", "CHN_ENG");
//        allPdfOcrAndTranslation("/Users/baichangda/pdftest", "CHN_ENG", "zh", "en");
        System.out.println(baiduInstance.translation("啊啊啊","zh","en"));

//        System.out.println(baiduInstance.vehicleDamage_imagePath("/Users/baichangda/Downloads/2.JPG").toPrettyString());

    }
}
