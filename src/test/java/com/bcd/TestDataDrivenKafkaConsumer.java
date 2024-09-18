package com.bcd;

import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.datadriven.DataDrivenKafkaConsumer;
import com.bcd.base.support_kafka.ext.datadriven.WorkExecutor;
import com.bcd.base.support_kafka.ext.datadriven.WorkHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDataDrivenKafkaConsumer extends DataDrivenKafkaConsumer {
    public TestDataDrivenKafkaConsumer() {
        super("Test",
                new ConsumerProp("192.168.23.129:9092", "test-bcd"),
                1,
                10000,
                0,
                100000,
                true,
                0,
                null,
                1,
                "test");
    }

    @Override
    public WorkHandler newHandler(String id, WorkExecutor executor) {
        return new TestHandler(id, executor);
    }

    public static void main(String[] args) {
        TestDataDrivenKafkaConsumer consumer = new TestDataDrivenKafkaConsumer();
        consumer.init();

//        for (int j = 0; j < 1; j++) {
//            Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
//                try (Producer<String, byte[]> producer = ProducerFactory.newProducer(new ProducerProp("192.168.23.129:9092"))) {
//                    while (true) {
//                        for (int i = 0; i < 100000; i++) {
//                            producer.send(new ProducerRecord<>("test", (i % 100) + "", (i + "").getBytes()));
//                        }
//                        TimeUnit.MILLISECONDS.sleep(1000);
//                    }
//                } catch (InterruptedException ex) {
//                    throw new RuntimeException(ex);
//                }
//            });
//        }
    }
}

class TestHandler extends WorkHandler {

    static Logger logger = LoggerFactory.getLogger(TestHandler.class);

    public TestHandler(String id, WorkExecutor executor) {
        super(id, executor);
    }

    @Override
    public void onMessage(ConsumerRecord<String, byte[]> msg) {
//        logger.info("onMessage: {}", new String(msg.value()));
//        try {
//            TimeUnit.MILLISECONDS.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}


