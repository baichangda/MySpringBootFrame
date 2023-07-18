package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExecutorUtil;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 */
public abstract class AbstractConsumer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 工作线程数量
     */
    private final int workThreadNum;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    private final int maxBlockingNum;

    /**
     * 当前阻塞数量
     */
    public final AtomicInteger blockingNum = new AtomicInteger();

    private final String[] topics;
    private final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue;
    private final Consumer<String, byte[]> consumer;

    /**
     * 最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     */
    private final int maxConsumeSpeed;
    private final AtomicInteger consumeCount;
    private ScheduledExecutorService resetConsumeCountPool;

    /**
     * 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     */
    private final boolean autoReleaseBlocking;
    private ExecutorService workPool;
    private volatile boolean running = true;

    /**
     * @param consumerProp        消费者属性
     * @param maxBlockingNum      最大阻塞数量
     * @param workThreadNum       工作线程个数
     * @param maxConsumeSpeed     最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @param autoReleaseBlocking 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param topics              消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            int workThreadNum,
                            int maxBlockingNum,
                            int maxConsumeSpeed,
                            boolean autoReleaseBlocking,
                            String... topics) {
        this.workThreadNum = workThreadNum;
        this.maxBlockingNum = maxBlockingNum;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.topics = topics;

        this.consumer = new KafkaConsumer<>(this.consumerProperties(consumerProp));
        this.queue = new ArrayBlockingQueue<>(maxBlockingNum);
        if (maxConsumeSpeed > 0) {
            this.consumeCount = new AtomicInteger();
        } else {
            this.consumeCount = null;
        }
    }

    /**
     * @param consumerProp   消费者属性
     * @param maxBlockingNum 最大阻塞数量
     * @param workThreadNum  工作线程个数
     * @param topics         消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            int workThreadNum,
                            int maxBlockingNum,
                            String... topics) {
        this(consumerProp, workThreadNum, maxBlockingNum, 0, false, topics);
    }


    public void init() {
        //订阅topic
        consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceLogger(consumer));
        //初始化重置消费计数线程池、提交工作任务、每秒重置消费数量
        if (maxConsumeSpeed > 0) {
            resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor();
            resetConsumeCountPool.scheduleAtFixedRate(() -> {
                consumeCount.getAndSet(0);
            }, 1, 1, TimeUnit.SECONDS);
        }
        //初始化工作线程池、提交工作任务
        workPool = Executors.newFixedThreadPool(workThreadNum);
        for (int i = 0; i < workThreadNum; i++) {
            workPool.execute(() -> {
                try {
                    onMessageInternal(queue.take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        //初始化消费线程、提交消费任务
        /**
         * 消费线程池、默认一个
         */
        final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();
        consumerExecutor.execute(this::consume);

        //销毁消费者、因为消费者执行的是死循环、只有当满足条件时候才会退出
        consumerExecutor.shutdown();
    }


    /**
     * 此操作仅仅是打上退出标记、不会马上结束
     * 销毁过程如下
     * 消费线程池任务检测到退出标记、退出循环、停止消费、然后销毁其他线程池资源{@link #destroyByConsumerExecutor()}
     */
    public void destroy() {
        //打上退出标记
        running = false;
    }

    private void destroyByConsumerExecutor() {
        //销毁消费线程池、销毁重置计数线程池(如果存在)
        ExecutorUtil.shutdownThenAwaitOneByOne(resetConsumeCountPool);
        //等待队列中为空、然后停止工作线程池、避免出现数据丢失
        ExecutorUtil.shutdownThenAwaitOneByOneAfterQueueEmpty(queue, workPool);
    }

    private Properties consumerProperties(ConsumerProp consumerProp) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProp.groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProp.bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProp.enableAutoCommit);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, (int) consumerProp.autoCommitInterval.toMillis());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, (int) consumerProp.heartbeatInterval.toMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProp.autoOffsetReset);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, (int) consumerProp.sessionTimeout.toMillis());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) consumerProp.requestTimeout.toMillis());
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, consumerProp.maxPartitionFetchBytes);
        return props;
    }


    /**
     * 用于在消费之后计数、主要用于性能统计
     *
     * @param count
     */
    protected void countAfterConsume(int count) {

    }

    private void checkConsumeSpeedAndSleep(int count) throws InterruptedException {
        //检查速度、如果速度太快则阻塞
        if (maxConsumeSpeed > 0) {
            //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
            final int curConsumeCount = consumeCount.addAndGet(count);
            if (curConsumeCount >= maxConsumeSpeed) {
                do {
                    TimeUnit.MILLISECONDS.sleep(10);
                } while (consumeCount.get() >= maxConsumeSpeed);
            }
        }
    }

    /**
     * 消费
     *
     * @return
     */
    public void consume() {
        while (running) {
            try {
                //检查阻塞
                if (blockingNum.get() >= maxBlockingNum) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }
                //消费一批数据
                ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(60));

                if (consumerRecords == null || consumerRecords.isEmpty()) {
                    continue;
                }

                //统计
                final int count = consumerRecords.count();
                blockingNum.addAndGet(count);

                countAfterConsume(count);

                //检查速度、如果速度太快则阻塞
                checkConsumeSpeedAndSleep(count);

                //发布消息
                for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                    //放入队列
                    queue.put(consumerRecord);
                }
            } catch (Exception ex) {
                logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error,Try Again After 3s", ex);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }
        logger.info("consumer[{}] exit", this.getClass().getName());
        //退出时候销毁其他资源
        destroyByConsumerExecutor();
        logger.info("consumer[{}] destroy", this.getClass().getName());
    }


    public abstract void onMessage(ConsumerRecord<String, byte[]> consumerRecord);

    private void onMessageInternal(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
            onMessage(consumerRecord);
        } catch (Exception ex) {
            logger.error("onMessageInternal error", ex);
        }
        if (autoReleaseBlocking) {
            blockingNum.decrementAndGet();
        }
    }
}

class ConsumerRebalanceLogger implements ConsumerRebalanceListener {
    static final Logger logger = LoggerFactory.getLogger(ConsumerRebalanceLogger.class);

    final Consumer<String, byte[]> consumer;

    public ConsumerRebalanceLogger(Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        String msg = partitions.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + ";" + e2).orElse("");
        logger.info("consumer[{}] onPartitionsRevoked [{}]", consumer.toString(), msg);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        String msg = partitions.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + ";" + e2).orElse("");
        logger.info("consumer[{}] onPartitionsAssigned [{}]", consumer.toString(), msg);
    }
}

