package com.bcd;

import com.bcd.nettyserver.http.NettyHttpServer;
import com.bcd.nettyserver.http.listener.TimeoutListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages="com.bcd")
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        Executors.newSingleThreadExecutor().execute(()->new NettyHttpServer("test",10001).run());
        //启动netty延时任务超时监听线程(每1s扫描一次)
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new TimeoutListener(), 3000L, 1000L, TimeUnit.MILLISECONDS);
    }
}