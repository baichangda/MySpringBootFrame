package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExecutorUtil;
import com.bcd.base.util.StringUtil;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 */
public abstract class AbstractConsumer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public final Properties properties;
    /**
     * 是否每个分区一个消费者
     */
    public final boolean consumerPerPartition;
    /**
     * 消费线程
     */
    public Thread consumeThread;
    public Thread[] consumeThreads;


    /**
     * 工作线程任务队列长度
     */
    public final int workThreadQueueSize;
    /**
     * 工作线程队列
     */
    public final ArrayBlockingQueue<ConsumerRecord<String, byte[]>>[] queues;
    public final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue;
    /**
     * 工作线程数量
     */
    public final int workThreadNum;
    /**
     * 工作线程数组
     */
    public final Thread[] workThreads;

    public final boolean workThreadPerQueue;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    public final int maxBlockingNum;

    /**
     * 当前阻塞数量
     */
    public final AtomicInteger blockingNum = new AtomicInteger();

    /**
     * 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     */
    public final boolean autoReleaseBlocking;

    /**
     * 最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     */
    public final int maxConsumeSpeed;
    public final AtomicInteger consumeCount;
    public ScheduledExecutorService resetConsumeCountPool;

    /**
     * 消费topic
     */
    public final String[] topics;

    /**
     * 当前消费者是否可用
     */
    public volatile boolean available;

    /**
     * 控制退出线程标志
     */
    public volatile boolean running_consume = true;
    public volatile boolean running_work = true;

    /**
     * 监控信息
     */
    public final int monitor_period;
    public final LongAdder monitor_consumeCount;
    public final LongAdder monitor_workCount;
    public ScheduledExecutorService monitor_pool;


    /**
     * @param consumerProp         消费者属性
     * @param consumerPerPartition 是否每个分区一个消费者
     *                             true时候、会首先通过{@link KafkaConsumer#partitionsFor(String)}获取分区个数、然后启动对应的消费线程、每一个线程一个消费者使用{@link KafkaConsumer#assign(Collection)}完成分配
     *                             false时候、会启动单线程即一个消费者使用{@link KafkaConsumer#subscribe(Pattern)}完成订阅
     * @param workThreadQueueSize  工作队列的长度
     * @param workThreadNum        工作线程个数
     * @param workThreadPerQueue   是否每一个工作线程都有一个队列
     *                             true时候、可以通过{@link #index(ConsumerRecord)}实现记录关联work线程、这在某些场景可以避免线程竞争
     *                             false时候、共享一个队列
     * @param maxBlockingNum       最大阻塞数量、当内存中达到最大阻塞数量时候、消费者会停止消费
     * @param autoReleaseBlocking  是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param maxConsumeSpeed      最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @param monitor_period       监控信息打印周期(秒)、0则代表不打印
     * @param topics               消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            boolean consumerPerPartition,
                            int workThreadQueueSize,
                            int workThreadNum,
                            boolean workThreadPerQueue,
                            int maxBlockingNum,
                            boolean autoReleaseBlocking,
                            int maxConsumeSpeed,
                            int monitor_period,
                            String... topics) {
        this.properties = consumerProp.toProperties();
        this.consumerPerPartition = consumerPerPartition;
        this.workThreadQueueSize = workThreadQueueSize;
        this.workThreadNum = workThreadNum;
        this.workThreadPerQueue = workThreadPerQueue;
        this.maxBlockingNum = maxBlockingNum;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.monitor_period = monitor_period;
        this.topics = topics;


        if (monitor_period == 0) {
            monitor_consumeCount = null;
            monitor_workCount = null;
        } else {
            monitor_consumeCount = new LongAdder();
            monitor_workCount = new LongAdder();
        }

        if (maxConsumeSpeed > 0) {
            consumeCount = new AtomicInteger();
        } else {
            consumeCount = null;
        }

        //初始化工作线程
        this.workThreads = new Thread[workThreadNum];
        //根据是否公用一个队列、来指定构造
        if (workThreadPerQueue) {
            this.queue = null;
            this.queues = new ArrayBlockingQueue[workThreadNum];
            for (int i = 0; i < workThreadNum; i++) {
                final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue = new ArrayBlockingQueue<>(workThreadQueueSize);
                this.queues[i] = queue;
                workThreads[i] = new Thread(() -> work(queue));
            }
        } else {
            this.queues = null;
            this.queue = new ArrayBlockingQueue<>(workThreadQueueSize);
            for (int i = 0; i < workThreadNum; i++) {
                workThreads[i] = new Thread(() -> work(this.queue));
            }
        }

    }

    public void init() {
        if (!available) {
            synchronized (this) {
                if (!available) {
                    try {
                        //标记可用
                        available = true;
                        //初始化重置消费计数线程池(如果有限制最大消费速度)、提交工作任务、每秒重置消费数量
                        if (maxConsumeSpeed > 0) {
                            resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor();
                            resetConsumeCountPool.scheduleAtFixedRate(() -> {
                                consumeCount.getAndSet(0);
                            }, 1, 1, TimeUnit.SECONDS);
                        }
                        //初始化工作线程池、提交工作任务
                        for (int i = 0; i < workThreadNum; i++) {
                            workThreads[i].start();
                        }
                        //启动监控
                        if (monitor_period != 0) {
                            monitor_pool = Executors.newSingleThreadScheduledExecutor();
                            monitor_pool.scheduleAtFixedRate(() -> logger.info(monitor_log()), monitor_period,monitor_period, TimeUnit.SECONDS);
                        }
                        //启动消费者
                        if (consumerPerPartition) {
                            try (final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties)) {
                                List<PartitionInfo> partitions = Arrays.stream(topics).flatMap(e -> consumer.partitionsFor(e).stream()).toList();
                                if (!partitions.isEmpty()) {
                                    consumeThreads = new Thread[partitions.size()];
                                    for (int i = 0; i < partitions.size(); i++) {
                                        PartitionInfo partitionInfo = partitions.get(i);
                                        try {
                                            final KafkaConsumer<String, byte[]> cur = new KafkaConsumer<>(properties);
                                            cur.assign(Collections.singletonList(new TopicPartition(partitionInfo.topic(), partitionInfo.partition())));
                                            consumeThreads[i] = new Thread(() -> consume(cur));
                                        } catch (Exception ex) {
                                            consumer.close();
                                            throw BaseRuntimeException.getException(ex);
                                        }
                                    }
                                    for (Thread thread : consumeThreads) {
                                        thread.start();
                                    }
                                }
                                logger.info("start consumers[{}] for partitions[{}]", partitions.size(), partitions.stream().map(e -> e.topic() + ":" + e.partition()).collect(Collectors.joining(",")));
                            }
                        } else {
                            final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                            consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceLogger(consumer));
                            //初始化消费线程、提交消费任务
                            consumeThread = new Thread(() -> consume(consumer));
                            consumeThread.start();
                            logger.info("start consumer for topics[{}]", String.join(",", topics));
                        }
                    } catch (Exception ex) {
                        //初始化异常、则销毁资源
                        destroy();
                        throw BaseRuntimeException.getException(ex);
                    }
                    //增加销毁回调
                    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
                }
            }
        }
    }


    public void destroy() {
        if (available) {
            synchronized (this) {
                if (available) {
                    //打上退出标记、等待消费线程退出
                    running_consume = false;
                    ExecutorUtil.shutdown(consumeThread, consumeThreads, resetConsumeCountPool, queue, queues);
                    //打上退出标记、等待工作线程退出
                    running_work = false;
                    ExecutorUtil.shutdown(workThreads, monitor_pool);
                    //标记不可用
                    available = false;
                }
            }
        }
    }

    final AtomicLong no = new AtomicLong();

    /**
     * 当{@link #workThreadPerQueue}为true时候生效
     * 可以根据数据内容绑定到对应的工作线程上、避免线程竞争
     *
     * @param consumerRecord
     * @return [0-{@link #workThreadNum})中某个值
     */
    protected int index(ConsumerRecord<String, byte[]> consumerRecord) {
        if (consumerRecord.key() == null) {
            return (int) ((no.getAndIncrement()) % workThreadNum);
        } else {
            return consumerRecord.key().hashCode() % workThreadNum;
        }
    }

    /**
     * 消费
     */
    private void consume(KafkaConsumer<String, byte[]> consumer) {
        try {
            if (workThreadPerQueue) {
                while (running_consume) {
                    try {
                        //检查阻塞
                        if (blockingNum.get() >= maxBlockingNum) {
                            TimeUnit.MILLISECONDS.sleep(100);
                            continue;
                        }
                        //消费一批数据
                        final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(3));
                        if (consumerRecords == null || consumerRecords.isEmpty()) {
                            continue;
                        }

                        //统计
                        final int count = consumerRecords.count();
                        blockingNum.addAndGet(count);
                        monitor_consumeCount.add(count);

                        //检查速度、如果速度太快则阻塞
                        if (maxConsumeSpeed > 0) {
                            //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
                            final int curConsumeCount = consumeCount.addAndGet(count);
                            if (curConsumeCount >= maxConsumeSpeed) {
                                do {
                                    TimeUnit.MILLISECONDS.sleep(50);
                                } while (consumeCount.get() >= maxConsumeSpeed);
                            }
                        }
                        //发布消息
                        for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                            //放入队列
                            queues[index(consumerRecord)].put(consumerRecord);
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
            } else {
                while (running_consume) {
                    try {
                        //检查阻塞
                        if (blockingNum.get() >= maxBlockingNum) {
                            TimeUnit.MILLISECONDS.sleep(100);
                            continue;
                        }
                        //消费一批数据
                        final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(60));

                        if (consumerRecords == null || consumerRecords.isEmpty()) {
                            continue;
                        }

                        //统计
                        final int count = consumerRecords.count();
                        blockingNum.addAndGet(count);

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
            }
        } finally {
            String assignment = consumer.assignment().stream().map(e -> e.topic() + ":" + e.partition()).collect(Collectors.joining(","));
            logger.info("consumer[{}] assignment[{}] close", this.getClass().getName(), assignment);
            consumer.close();
        }

    }

    /**
     * 工作线程
     *
     * @param queue
     */
    private void work(final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue) {
        try {
            while (running_work) {
                final ConsumerRecord<String, byte[]> poll = queue.poll(1, TimeUnit.SECONDS);
                if (poll != null) {
                    try {
                        onMessage(poll);
                    } catch (Exception ex) {
                        logger.error("work error", ex);
                    }
                    monitor_workCount.increment();
                    if (autoReleaseBlocking) {
                        blockingNum.decrementAndGet();
                    }
                }
            }
        } catch (InterruptedException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }


    public abstract void onMessage(ConsumerRecord<String, byte[]> consumerRecord) throws Exception;


    /**
     * 监控日志
     * 如果需要修改日志、可以重写此方法
     */
    public String monitor_log() {
        return StringUtil.format("blocking[{}/{}] consume[{}/s] queues[{}] work[{}/s]",
                blockingNum.get(), maxBlockingNum,
                monitor_consumeCount.sumThenReset() / monitor_period,
                workThreadPerQueue ? Arrays.stream(queues).map(e -> e.size() + "/" + workThreadQueueSize).collect(Collectors.joining(",")) : queue.size(),
                monitor_workCount.sumThenReset() / monitor_period);
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

