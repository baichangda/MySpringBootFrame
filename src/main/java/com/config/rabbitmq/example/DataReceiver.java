package com.config.rabbitmq.example;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class DataReceiver {
    @RabbitListener(queues = "dataQueue")
    public void process(JSONObject data) {
        System.out.println("Receiver  : " + data);
        saveDataToFile(data.toJSONString());
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