package com.bcd.config.aliyun.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

//@Configuration
//@EnableConfigurationProperties(AliyunProperties.class)
public class SmsConfig {
    @Autowired
    private AliyunProperties aliyunProperties;

    @Bean
    public IAcsClient iAcsClient() throws Exception{
        //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        IClientProfile profile = DefaultProfile.getProfile(aliyunProperties.sms.regionId, aliyunProperties.accessKeyId,
                aliyunProperties.accessKeySecret);
        DefaultProfile.addEndpoint(aliyunProperties.sms.endpointName,aliyunProperties.sms.product,aliyunProperties.sms.regionId);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        return acsClient;
    }
}
