package com.bcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages= {"com.bcd"})
@EnableAspectJAutoProxy(proxyTargetClass = true,exposeProxy = true)
public class Application{

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}