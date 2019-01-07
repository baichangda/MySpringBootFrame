package com.bcd.config.aliyun.rocketmq;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

//@Component
//@EnableConfigurationProperties(AliyunProperties.class)
public class MyProducerBean extends ProducerBean{
    @Autowired
    private AliyunProperties aliyunProperties;
    public MyProducerBean(){
        Properties properties=new Properties();
        properties.setProperty(PropertyKeyConst.ProducerId,aliyunProperties.rocketMq.producerId);
        properties.setProperty(PropertyKeyConst.AccessKey,aliyunProperties.accessKeyId);
        properties.setProperty(PropertyKeyConst.SecretKey,aliyunProperties.accessKeySecret);
        properties.setProperty(PropertyKeyConst.ONSAddr,aliyunProperties.rocketMq.onsAddr);
        setProperties(properties);
    }

    @PostConstruct
    @Override
    public void start(){
        super.start();
    }

    @PreDestroy
    @Override
    public void shutdown(){
        super.shutdown();
    }


}
