package com.bcd.config.aliyun.mns;

import com.aliyun.mns.client.MNSClient;
import com.bcd.config.aliyun.mns.consumer.example.Test1Consumer;
import com.bcd.config.aliyun.mns.consumer.example.Test2Consumer;
import com.bcd.config.aliyun.properties.AliyunProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Component
public class MnsBoot implements CommandLineRunner{

    @Autowired
    private AliyunProperties aliyunProperties;

    @Autowired
    private MNSClient mnsClient;

    @Override
    public void run(String... args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new Test1Consumer(mnsClient,aliyunProperties));
        executor.execute(new Test2Consumer(mnsClient,aliyunProperties));
    }


}
