package com.bcd;

import com.bcd.base.support_kafka.nospring.AbstractConsumer;
import com.bcd.base.support_kafka.nospring.ConsumerProp;
import com.bcd.base.support_kafka.nospring.ProducerFactory;
import com.bcd.base.support_kafka.nospring.ProducerProp;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = Application.class)
public class TestAbstractConsumer extends AbstractConsumer {
    public TestAbstractConsumer() {
        super(new ConsumerProp("192.168.23.128:9092", "test-bcd"), true,true, 100000, 1, 100000, true, 0, 1, "test");
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
        TestAbstractConsumer consumer = new TestAbstractConsumer();
        consumer.init();

        for (int j = 0; j < 2; j++) {
            Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
                try (Producer<String, byte[]> producer = ProducerFactory.newProducer(new ProducerProp("192.168.23.128:9092"))) {
                    while (true) {
                        for (int i = 0; i < 100000; i++) {
                            producer.send(new ProducerRecord<>("test", (i + "").getBytes()));
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
