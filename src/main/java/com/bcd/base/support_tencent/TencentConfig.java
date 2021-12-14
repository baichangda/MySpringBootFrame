package com.bcd.base.support_tencent;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.Region;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Configuration
public class TencentConfig {


    @Bean
    public Credential credential() {
        Credential credential = new Credential("AKIDIPFo79uObYDbiY76oFQKwHMAhwuc3C4g",
                "NTIr9AuNZT6SYNuAAtY5NQEm47uRikX4");
        return credential;
    }

    @Bean
    public OcrClient ocrClient() {
        return new OcrClient(credential(), Region.Beijing.getValue());
    }

    public static void main(String[] args) throws IOException, TencentCloudSDKException {
        TencentConfig tencentConfig=new TencentConfig();

        RecognizeTableOCRRequest recognizeTableOCRRequest=new RecognizeTableOCRRequest();
        recognizeTableOCRRequest.setIsPdf(true);
        recognizeTableOCRRequest.setImageBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("/Users/baichangda/pdftemp/1.PNG"))));
        final RecognizeTableOCRResponse recognizeTableOCRResponse = tencentConfig.ocrClient().RecognizeTableOCR(recognizeTableOCRRequest);
        Files.write(Paths.get("/Users/baichangda/pdftemp/1.xlsx"),Base64.getDecoder().decode(recognizeTableOCRResponse.getData()));

//        SmartStructuralOCRRequest smartStructuralOCRRequest=new SmartStructuralOCRRequest();
//        smartStructuralOCRRequest.setImageBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("/Users/baichangda/pdftemp/1.PNG"))));
//        final SmartStructuralOCRResponse smartStructuralOCRResponse = tencentConfig.ocrClient().SmartStructuralOCR(smartStructuralOCRRequest);
//        for (StructuralItem structuralItem : smartStructuralOCRResponse.getStructuralItems()) {
//            System.out.println(structuralItem);
//        }
    }


}
