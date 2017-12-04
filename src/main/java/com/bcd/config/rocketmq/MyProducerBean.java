package com.bcd.config.rocketmq;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

//@Component
public class MyProducerBean extends ProducerBean{
    public MyProducerBean(){
        Properties properties=new Properties();
        properties.setProperty(PropertyKeyConst.ProducerId,"PID_Proudcer_4");
        properties.setProperty(PropertyKeyConst.AccessKey,"LTAI2LEtG5xmDgUx");
        properties.setProperty(PropertyKeyConst.SecretKey,"CgVFaBhk8znSdBsZVlBSFSqr4a76P1");
        properties.setProperty(PropertyKeyConst.ONSAddr,
                "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");
        setProperties(properties);
    }

    @PostConstruct
    public void start(){
        super.start();
    }

    @PreDestroy
    public void shutdown(){
        super.shutdown();
    }


}
