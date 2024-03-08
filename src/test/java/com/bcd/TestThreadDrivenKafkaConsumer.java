package com.bcd;

import com.bcd.base.support_kafka.ext.simple.ThreadDrivenKafkaConsumer;
import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.ProducerFactory;
import com.bcd.base.support_kafka.ext.ProducerProp;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = Application.class)
public class TestThreadDrivenKafkaConsumer extends ThreadDrivenKafkaConsumer {
    public TestThreadDrivenKafkaConsumer() {
        super("Test",new ConsumerProp("192.168.23.128:9092", "test-bcd"), true,false, 100000, 1, 100000, true, 0, 1, "test");
    }

    @Override
    public void onMessage(ConsumerRecord<String, byte[]> consumerRecord) {
//        logger.info("onMessage: {}", new String(consumerRecord.value()));
//        try {
//            TimeUnit.MILLISECONDS.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static void main(String[] args) {
        TestThreadDrivenKafkaConsumer consumer = new TestThreadDrivenKafkaConsumer();
        consumer.init();

        for (int j = 0; j < 2; j++) {
            Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
                try (Producer<String, byte[]> producer = ProducerFactory.newProducer(new ProducerProp("192.168.23.128:9092"))) {
                    while (true) {
                        for (int i = 0; i < 100000; i++) {
                            producer.send(new ProducerRecord<>("test",(i % 100) + "", (i + "").getBytes()));
                        }
                        TimeUnit.MILLISECONDS.sleep(5);
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

    }
}
