package com.bcd.base.support_tencent;

import com.bcd.base.support_jpa.dbinfo.data.DBInfo;
import com.bcd.base.util.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.Region;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Configuration
public class TencentConfig {

    @Value("${tencent.secretId}")
    String secretId;
    @Value("${tencent.secretKey}")
    String secretKey;

    @Bean
    public Credential credential() {
        Credential credential = new Credential(secretId, secretKey);
        return credential;
    }

    @Bean
    public OcrClient ocrClient() {
        return new OcrClient(credential(), Region.Beijing.getValue());
    }

    public static void main(String[] args) throws IOException, TencentCloudSDKException {
        TencentConfig tencentConfig = new TencentConfig();
        final JsonNode[] props = SpringUtil.getSpringProps("tencent.secretId", "tencent.secretKey");
        tencentConfig.secretId=props[0].asText();
        tencentConfig.secretKey=props[1].asText();

        RecognizeTableOCRRequest recognizeTableOCRRequest = new RecognizeTableOCRRequest();
        recognizeTableOCRRequest.setIsPdf(true);
        recognizeTableOCRRequest.setImageBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("/Users/baichangda/pdftemp/1.PNG"))));
        final RecognizeTableOCRResponse recognizeTableOCRResponse = tencentConfig.ocrClient().RecognizeTableOCR(recognizeTableOCRRequest);
        Files.write(Paths.get("/Users/baichangda/pdftemp/1.xlsx"), Base64.getDecoder().decode(recognizeTableOCRResponse.getData()));

//        SmartStructuralOCRRequest smartStructuralOCRRequest=new SmartStructuralOCRRequest();
//        smartStructuralOCRRequest.setImageBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("/Users/baichangda/pdftemp/1.PNG"))));
//        final SmartStructuralOCRResponse smartStructuralOCRResponse = tencentConfig.ocrClient().SmartStructuralOCR(smartStructuralOCRRequest);
//        for (StructuralItem structuralItem : smartStructuralOCRResponse.getStructuralItems()) {
//            System.out.println(structuralItem);
//        }
    }


}
