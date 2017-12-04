package com.bcd.config.rocketmq;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.bcd.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
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
