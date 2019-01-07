package com.bcd.config.rabbitmq.example;

import com.bcd.base.exception.BaseRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


public class DataReceiver {
    private final static Logger logger= LoggerFactory.getLogger(DataReceiver.class);
    @RabbitListener(queues = "dataQueue")
    public void process(Map<String,Object> data) {
        String dataStr= null;
        try {
            dataStr = new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw BaseRuntimeException.getException(e);
        }
        logger.debug("Receiver  : {}" , dataStr);
    }
}