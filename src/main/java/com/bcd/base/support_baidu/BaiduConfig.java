package com.bcd.base.support_baidu;

import com.baidu.aip.ocr.AipOcr;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaiduConfig {
    @Bean
    public BaiduInstance baiduInstance() {
        return BaiduInstance.newInstance("sZnWtPyo8VpnG3TPVy6pIgYg", "bI7HokxcdbvMfpY0I3mL6vB2GqsSlxbk");
    }

    @Bean
    public AipOcr aipOcr() {
        return new AipOcr("test","sZnWtPyo8VpnG3TPVy6pIgYg", "bI7HokxcdbvMfpY0I3mL6vB2GqsSlxbk");
    }
}
