package com.bcd.base.support_baidu;

import com.baidu.aip.http.AipRequest;
import com.baidu.aip.imageclassify.AipImageClassify;
import com.baidu.aip.ocr.AipOcr;
import com.baidu.aip.ocr.OcrConsts;
import com.baidu.aip.util.Base64Util;
import com.bcd.base.support_jpa.dbinfo.mysql.util.DBInfoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;


@Configuration
public class BaiduConfig {
    @Value("${baidu.apiKey}")
    String apiKey;
    @Value("${baidu.secretKey}")
    String secretKey;
    @Bean
    public AipImageClassify aipImageClassify(){
        return new AipImageClassify("wx-bcd-aipImageClassify",apiKey,secretKey);
    }

    public static void main(String[] args) throws IOException {
        final JsonNode[] props = DBInfoUtil.getSpringProps("baidu.apiKey", "baidu.secretKey");
        AipOcr aipOcr=new AipOcr("wx-bcd-aipOcr",props[0].asText(),props[1].asText())
//        {
//            @Override
//            public JSONObject basicAccurateGeneral(byte[] image, HashMap<String, String> options) {
//                AipRequest request = new AipRequest();
//                preOperation(request);
//
//                String base64Content = Base64Util.encode(image);
//                request.addBody("pdf_file", base64Content);
//                if (options != null) {
//                    request.addBody(options);
//                }
//                request.setUri("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic");
//                postOperation(request);
//                return requestServer(request);
//            }
//        }
        ;
        HashMap<String, String> options=new HashMap<>();
        options.put("language_type","RUS");
//        options.put("pdf_file","RUS");
        final JSONObject jsonObject = aipOcr.basicAccurateGeneral("/Users/baichangda/test.png", options);
//        final JSONObject jsonObject = aipOcr.basicGeneral("/Users/baichangda/test.png", options);
//        final JSONObject jsonObject = aipOcr.basicAccurateGeneral("/Users/baichangda/test.pdf", options);
        for (Object words_result : jsonObject.getJSONArray("words_result")) {
            System.err.println(((JSONObject)words_result).get("words"));
        }

    }
}
