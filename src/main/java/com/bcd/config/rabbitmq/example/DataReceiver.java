package com.bcd.config.rabbitmq.example;

import com.bcd.base.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.Map;


public class DataReceiver {
    private final static Logger logger= LoggerFactory.getLogger(DataReceiver.class);
    @RabbitListener(queues = "dataQueue")
    public void process(Map<String,Object> data) {
        String dataStr= JsonUtil.toJson(data);
        logger.debug("Receiver  : {}" , dataStr);
    }
}