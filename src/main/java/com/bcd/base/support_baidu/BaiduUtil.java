package com.bcd.base.support_baidu;

import com.baidu.aip.ocr.AipOcr;
import com.bcd.base.exception.BaseRuntimeException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;


public class BaiduUtil {

    static Logger logger = LoggerFactory.getLogger(BaiduUtil.class);

    private final static String clientId = "sZnWtPyo8VpnG3TPVy6pIgYg";
    private final static String clientSecret = "bI7HokxcdbvMfpY0I3mL6vB2GqsSlxbk";
    private final static BaiduInstance baiduInstance = BaiduInstance.newInstance(clientId, clientSecret);
    private final static AipOcr aipOcr = new AipOcr("test",clientId, clientSecret);

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
                    final byte[] bytes;
                    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        ImageIO.write(renderer.renderImageWithDPI(i, 300), "png", os);
                        bytes = os.toByteArray();
//                        Files.write(Paths.get("/Users/baichangda/pdftemp/" + fileName.substring(0, fileName.lastIndexOf(".")) + i + ".png"), bytes);
                    }
                    //调用百度识别
                    JSONObject jsonObject = null;
                    try {
                        while (true) {
                            HashMap<String, String> options =new HashMap<>();
                            options.put("language_type",languageType);
                            jsonObject = aipOcr.accurateGeneral(Base64.getDecoder().decode(bytes),options);
                            if (jsonObject.has("error_code")) {
                                break;
                            } else {
                                if (jsonObject.getInt("error_code") == 18) {
                                    logger.info("handle total[{}] pageNum[{}] call baidu error,sleep 5s and retry:\n{}", pageSize, i + 1, jsonObject);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        throw BaseRuntimeException.getException(ex);
                                    }
                                } else {
                                    logger.info("handle total[{}] pageNum[{}] call baidu error,skip page:\n{}",  pageSize, i + 1, jsonObject);
                                    jsonObject = null;
                                    break;
                                }
                            }
                        }
                        bw.newLine();
                        bw.write("================" + (i + 1) + "=================");
                        bw.newLine();
                        bw.newLine();
                        bw.flush();
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
//        allPdfOcr("/Users/baichangda/pdftemp", "CHN_ENG");

        HashMap<String, String> options=new HashMap<>();
        final JSONObject jsonObject = aipOcr.form("/Users/baichangda/pdftemp/1.PNG",options);
        System.out.println(jsonObject);
    }
}
