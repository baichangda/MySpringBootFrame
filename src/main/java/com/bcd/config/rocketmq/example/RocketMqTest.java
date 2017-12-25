package com.bcd.config.rocketmq.example;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.bcd.config.rocketmq.MyProducerBean;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RocketMqTest {
    @Autowired
    private MyProducerBean producer;
    @Test
    public void test(){
        Message message=new Message("Test_4","1","key","hello".getBytes());
        SendResult res=producer.send(message);
        System.out.println(res);
    }
}
