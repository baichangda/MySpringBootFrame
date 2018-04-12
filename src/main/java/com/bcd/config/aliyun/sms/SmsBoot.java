package com.bcd.config.aliyun.sms;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class SmsBoot implements CommandLineRunner {
    @Autowired
    private IAcsClient iAcsClient;
    @Override
    public void run(String... args) throws Exception {
        SmsUtil.init(iAcsClient);
        test();
    }

    private void test(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name","baichangda");
        SmsMessageBean smsMessageBean=new SmsMessageBean("13720278557","阿里云短信测试专用","SMS_127151264",jsonObject);
        SendSmsResponse resp =SmsUtil.sendMessage(smsMessageBean);
        System.err.println(resp.getCode());
    }
}
