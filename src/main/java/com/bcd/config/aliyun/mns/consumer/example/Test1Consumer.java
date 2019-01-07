package com.bcd.config.aliyun.mns.consumer.example;

import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.Message;
import com.bcd.config.aliyun.mns.consumer.AbstractMnsConsumer;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 下行控制 车身数据信息 消费者
 */
public class Test1Consumer extends AbstractMnsConsumer {
    private final static Logger logger = LoggerFactory.getLogger(Test1Consumer.class) ;


    public Test1Consumer(MNSClient mnsClient, AliyunProperties aliyunProperties) {
        super(mnsClient, aliyunProperties.mns.test1);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handle(Message popMsg) {
        System.err.println(popMsg.getMessageBodyAsString());
    }

}
