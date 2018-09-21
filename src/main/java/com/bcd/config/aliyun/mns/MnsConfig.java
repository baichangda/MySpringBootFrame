package com.bcd.config.aliyun.mns;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.MNSClient;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;


//@Configuration
//@EnableConfigurationProperties(AliyunProperties.class)
public class MnsConfig {

    @Autowired
    private AliyunProperties aliyunProperties;

    @Bean
    public MNSClient mnsClient(){
        CloudAccount account = new CloudAccount(aliyunProperties.accessKeyId,
                aliyunProperties.accessKeySecret, aliyunProperties.mns.endPoint);
        MNSClient client = account.getMNSClient();
        return client;
    }
}
