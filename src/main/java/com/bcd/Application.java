package com.bcd;

import com.bcd.nettyserver.http.NettyHttpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="com.bcd")
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        new NettyHttpServer("test",10001).run();
    }
}