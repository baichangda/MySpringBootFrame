package com.bcd.base.support_baidu;

import com.baidu.aip.ocr.AipOcr;
import com.bcd.base.exception.BaseRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class BaiduUtil {

    static Logger logger = LoggerFactory.getLogger(BaiduUtil.class);

    public static void pdfText(String pdfPath) throws IOException {
        PDDocument doc = PDDocument.load(Paths.get(pdfPath).toFile());
        PDFTextStripper pdfTextStripper = new PDFTextStripper(){
            @Override
            protected void processTextPosition(TextPosition text) {
                super.processTextPosition(text);
                System.out.print(text);
            }
        };
        final String text = pdfTextStripper.getText(doc);
        logger.info("======={}",text);
    }

    /**
     * 识别所有pdf下面的文字
     *
     * @param aipOcr 百度ocr对象
     * @param pdfDirPath pdf文件夹路径
     * @param languageType 识别语言
     *
     */
    public static void allPdfOcr(AipOcr aipOcr, String pdfDirPath, String languageType) throws IOException {
        final List<Path> filePathList = Files.list(Paths.get(pdfDirPath)).filter(e -> e.getFileName().toString().endsWith(".pdf")).collect(Collectors.toList());
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
                        ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", os);
                        bytes = os.toByteArray();
                        Files.write(Paths.get("/Users/baichangda/pdftemp/" + fileName.substring(0, fileName.lastIndexOf(".")) + i + ".png"), bytes);
                    }
                    //调用百度识别
                    JSONObject jsonObject = null;
                    try {
                        while (true) {
                            HashMap<String, String> options = new HashMap<>();
                            options.put("language_type", languageType);
                            jsonObject = aipOcr.basicAccurateGeneral(bytes, options);
                            if (jsonObject.has("error_code")) {
                                if (jsonObject.getInt("error_code") == 18) {
                                    logger.info("handle total[{}] pageNum[{}] call baidu error,sleep 5s and retry:\n{}", pageSize, i + 1, jsonObject);
                                    try {
                                        TimeUnit.SECONDS.sleep(5);
                                    } catch (InterruptedException ex) {
                                        throw BaseRuntimeException.getException(ex);
                                    }
                                } else {
                                    logger.info("handle total[{}] pageNum[{}] call baidu error,skip page:\n{}", pageSize, i + 1, jsonObject);
                                    jsonObject = null;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (jsonObject != null) {
                            final JSONArray words_result = jsonObject.getJSONArray("words_result");
                            for (int j = 0; j < words_result.length(); j++) {
                                bw.write(words_result.getJSONObject(j).getString("words"));
                                bw.newLine();
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
//        final JsonNode[] props = SpringUtil.getSpringProps("baidu.secretId", "baidu.secretKey");
//        String secretId = props[0].asText();
//        String secretKey = props[1].asText();
//        AipOcr aipOcr = new AipOcr("test", secretId, secretKey);
//        final BaiduInstance baiduInstance = BaiduInstance.newInstance(secretId, secretKey);
//        allPdfOcr(aipOcr,"/Users/baichangda/pdftemp", "CHN_ENG");

//        final JSONObject jsonObject = aipOcr.tableRecognizeToExcelUrl("/Users/baichangda/pdftemp/1.PNG", 100000);
//        System.out.println(jsonObject);

        pdfText("/Users/baichangda/pdftemp/1.pdf");

    }
}
