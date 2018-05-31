package com.bcd.config.rabbitmq.example;

import com.bcd.base.exception.BaseRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


public class DataReceiver {
    @RabbitListener(queues = "dataQueue")
    public void process(Map<String,Object> data) {
        System.out.println("Receiver  : " + data);
        try {
            saveDataToFile(new ObjectMapper().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    private void saveDataToFile(String data) {
        File file = new File("d:/ldData.txt");
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(data);
            pw.flush();
        } catch (IOException e) {

        }
    }

}