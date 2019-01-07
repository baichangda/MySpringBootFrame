package com.bcd.config.aliyun.sms;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.Map;

//@Component
public class SmsBoot implements CommandLineRunner {
    private final static Logger logger= LoggerFactory.getLogger(SmsBoot.class);
    @Autowired
    private IAcsClient iAcsClient;
    @Override
    public void run(String... args) throws Exception {
        SmsUtil.init(iAcsClient);
        test();
    }

    private void test(){
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put("name","baichangda");
        SmsMessageBean smsMessageBean=new SmsMessageBean("13720278557","阿里云短信测试专用","SMS_127151264",dataMap);
        SendSmsResponse resp =SmsUtil.sendMessage(smsMessageBean);
        logger.debug(resp.getCode());
    }
}
