package com.bcd.config.aliyun.tablestore;

import com.alicloud.openservices.tablestore.ClientConfiguration;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.AlwaysRetryStrategy;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

//@Configuration
//@EnableConfigurationProperties(AliyunProperties.class)
public class TableStoreConfig {

    @Autowired
    private AliyunProperties aliyunProperties;

    @Bean
    public SyncClient syncClient(){
        // ClientConfiguration提供了很多配置项，以下只列举部分。
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        // 设置建立连接的超时时间。
        clientConfiguration.setConnectionTimeoutInMillisecond(5000);
        // 设置socket超时时间。
        clientConfiguration.setSocketTimeoutInMillisecond(5000);
        // 设置重试策略，若不设置，采用默认的重试策略。
        clientConfiguration.setRetryStrategy(new AlwaysRetryStrategy());
        SyncClient client = new SyncClient(
                aliyunProperties.tableStore.endPoint,
                aliyunProperties.accessKeyId,
                aliyunProperties.accessKeySecret,
                aliyunProperties.tableStore.instanceName,
                clientConfiguration);
        return client;
    }
}
