package com.bcd.base.support_kafka.ext.datadriven;

import com.bcd.base.exception.BaseException;
import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.ConsumerRebalanceLogger;
import com.bcd.base.util.ExecutorUtil;
import com.bcd.base.util.StringUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 * 数据驱动模型
 * 即消费到数据后、每一个数据将会根据{@link #id(ConsumerRecord)}、{@link #getWorkExecutor(String)}分配到固定的{@link WorkExecutor}
 * 同时会通过{@link #newHandler(String, WorkExecutor)}构造数据对象
 * 后续{@link WorkHandler}中所有的操作都会由分配的{@link WorkExecutor}来执行
 * 这样做的好处是能保证{@link WorkHandler}所有方法都是线程安全的
 */
public abstract class DataDrivenKafkaConsumer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public final String name;
    public final Properties properties;
    /**
     * 是否每个分区一个消费者
     */
    public final boolean onePartitionOneConsumer;
    /**
     * 消费线程
     */
    public Thread consumeThread;
    public Thread[] consumeThreads;

    /**
     * 工作执行器数量
     */
    public final int workExecutorNum;
    /**
     * 工作执行器非阻塞任务线程池队列大小
     */
    public final int workExecutorQueueSize;
    /**
     * 工作执行器数组
     */
    public final WorkExecutor[] workExecutors;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    public final int maxBlockingNum;

    /**
     * 当前阻塞数量
     */
    public final LongAdder blockingNum = new LongAdder();

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
    /**
     * 监控信息
     */
    public final int monitor_period;
    public final LongAdder monitor_workHandlerCount;
    public final LongAdder monitor_consumeCount;
    public final LongAdder monitor_workCount;
    public ScheduledExecutorService monitor_pool;

    private Thread shutdownHookThread;

    /**
     * 构造一个默认的消费者
     *
     * @param name
     * @param consumerProp
     * @param topics
     */
    public DataDrivenKafkaConsumer(String name, ConsumerProp consumerProp, String... topics) {
        this(name, consumerProp,
                false,
                Runtime.getRuntime().availableProcessors(),
                0,
                10000,
                true,
                0,
                3,
                topics);
    }


    /**
     * @param name                    当前消费者的名称(用于标定线程名称)
     *                                消费者线程开头 {name}-consumer
     *                                工作任务执行器线程开头 {name}-worker
     *                                - 每个executor中有两种线程、分别用于运行非阻塞逻辑(无后缀)、阻塞逻辑(以-blocking后缀结尾)
     *                                监控线程开头 {name}-monitor
     * @param consumerProp            消费者属性
     * @param onePartitionOneConsumer 一个分区一个消费者
     *                                true时候、会首先通过{@link KafkaConsumer#partitionsFor(String)}获取分区个数、然后启动对应的消费线程、每一个线程一个消费者使用{@link KafkaConsumer#assign(Collection)}完成分配
     *                                false时候、会启动单线程即一个消费者使用{@link KafkaConsumer#subscribe(Pattern)}完成订阅
     * @param workExecutorNum         工作任务执行器个数
     * @param workExecutorQueueSize   工作任务执行器无阻塞任务线程池队列大小
     *                                <=0代表不限制、此时使用{@link LinkedBlockingQueue}
     *                                其他情况、则使用{@link ArrayBlockingQueue}
     * @param maxBlockingNum          最大阻塞数量(0代表不限制)、当内存中达到最大阻塞数量时候、消费者会停止消费
     *                                当不限制时候、还是会记录{@link #blockingNum}、便于监控阻塞数量
     * @param autoReleaseBlocking     是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param maxConsumeSpeed         最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     *                                每消费一次的数据量大小取决于如下消费者参数
     *                                {@link ConsumerConfig#MAX_POLL_RECORDS_CONFIG} 一次poll消费最大数据量
     *                                {@link ConsumerConfig#MAX_PARTITION_FETCH_BYTES_CONFIG} 每个分区最大拉取字节数
     * @param monitor_period          监控信息打印周期(秒)、0则代表不打印
     * @param topics                  消费的topic
     */
    public DataDrivenKafkaConsumer(String name,
                                   ConsumerProp consumerProp,
                                   boolean onePartitionOneConsumer,
                                   int workExecutorNum,
                                   int workExecutorQueueSize,
                                   int maxBlockingNum,
                                   boolean autoReleaseBlocking,
                                   int maxConsumeSpeed,
                                   int monitor_period,
                                   String... topics) {
        this.name = name;
        this.properties = consumerProp.toProperties();
        this.onePartitionOneConsumer = onePartitionOneConsumer;
        this.workExecutorNum = workExecutorNum;
        this.workExecutorQueueSize = workExecutorQueueSize;
        this.maxBlockingNum = maxBlockingNum;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.monitor_period = monitor_period;
        this.topics = topics;

        if (monitor_period == 0) {
            monitor_workHandlerCount = null;
            monitor_consumeCount = null;
            monitor_workCount = null;
        } else {
            monitor_workHandlerCount = new LongAdder();
            monitor_consumeCount = new LongAdder();
            monitor_workCount = new LongAdder();
        }

        if (maxConsumeSpeed > 0) {
            consumeCount = new AtomicInteger();
        } else {
            consumeCount = null;
        }

        //初始化工作任务执行器
        this.workExecutors = new WorkExecutor[workExecutorNum];
        for (int i = 0; i < workExecutorNum; i++) {
            this.workExecutors[i] = new WorkExecutor(name + "-worker(" + workExecutorNum + ")-" + i, workExecutorQueueSize);
        }
    }

    public WorkExecutor getWorkExecutor(String id) {
        int index = Math.floorMod(id.hashCode(), workExecutorNum);
        return workExecutors[index];
    }

    public abstract WorkHandler newHandler(String id, WorkExecutor executor);

    public final CompletableFuture<Void> removeHandler(String id) {
        WorkExecutor workExecutor = getWorkExecutor(id);
        return workExecutor.execute(() -> {
            WorkHandler workHandler = workExecutor.workHandlers.remove(id);
            if (workHandler != null) {
                workHandler.destroy();
                monitor_workHandlerCount.decrement();
            }
        });
    }

    public final WorkHandler getHandler(String id) {
        WorkExecutor workExecutor = getWorkExecutor(id);
        try {
            return workExecutor.submit(() -> workExecutor.workHandlers.get(id)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw BaseException.get(e);
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
                                consumeCount.set(0);
                            }, 1, 1, TimeUnit.SECONDS);
                        }
                        //启动监控
                        if (monitor_period != 0) {
                            monitor_pool = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, name + "-monitor"));
                            monitor_pool.scheduleAtFixedRate(() -> logger.info(monitor_log()), monitor_period, monitor_period, TimeUnit.SECONDS);
                        }
                        //启动消费者
                        if (onePartitionOneConsumer) {
                            final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                            List<PartitionInfo> partitions = Arrays.stream(topics).flatMap(e -> consumer.partitionsFor(e).stream()).toList();
                            if (partitions.isEmpty()) {
                                consumer.close();
                            } else {
                                int partitionSize = partitions.size();
                                KafkaConsumer<String, byte[]>[] consumers = new KafkaConsumer[partitionSize];
                                try {
                                    PartitionInfo firstPartitionInfo = partitions.get(0);
                                    consumer.assign(Collections.singletonList(new TopicPartition(firstPartitionInfo.topic(), firstPartitionInfo.partition())));
                                    consumers[0] = consumer;
                                    for (int i = 1; i < partitionSize; i++) {
                                        PartitionInfo partitionInfo = partitions.get(i);
                                        final KafkaConsumer<String, byte[]> cur = new KafkaConsumer<>(properties);
                                        cur.assign(Collections.singletonList(new TopicPartition(partitionInfo.topic(), partitionInfo.partition())));
                                        consumers[i] = cur;
                                    }
                                } catch (Exception ex) {
                                    //发生异常则关闭之前构造的消费者
                                    for (KafkaConsumer<String, byte[]> cur : consumers) {
                                        if (cur != null) {
                                            cur.close();
                                        }
                                    }
                                    throw BaseException.get(ex);
                                }
                                consumeThreads = new Thread[partitionSize];
                                for (int i = 0; i < partitionSize; i++) {
                                    final KafkaConsumer<String, byte[]> cur = consumers[i];
                                    Thread thread = new Thread(() -> consume(cur), name + "-consumer" + "(" + partitionSize + ")-" + i);
                                    consumeThreads[i] = thread;
                                    thread.start();
                                }
                            }
                            logger.info("start consumers[{}] for partitions[{}]", partitions.size(), partitions.stream().map(e -> e.topic() + ":" + e.partition()).collect(Collectors.joining(",")));
                        } else {
                            final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                            consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceLogger(consumer));
                            //初始化消费线程、提交消费任务
                            consumeThread = new Thread(() -> consume(consumer), name + "-consumer(1)-0");
                            consumeThread.start();
                            logger.info("start consumer for topics[{}]", String.join(",", topics));
                        }
                        //增加销毁回调
                        shutdownHookThread = new Thread(this::destroy);
                        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
                    } catch (Exception ex) {
                        //初始化异常、则销毁资源
                        destroy();
                        throw BaseException.get(ex);
                    }
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
                    ExecutorUtil.shutdownThenAwait(consumeThread, consumeThreads, resetConsumeCountPool);
                    //等待工作执行器退出
                    for (WorkExecutor workExecutor : workExecutors) {
                        //先销毁所有handler
                        workExecutor.execute(() -> {
                            Set<String> keySet = workExecutor.workHandlers.keySet();
                            for (String key : keySet) {
                                removeHandler(key);
                            }
                        });
                        workExecutor.destroy();
                    }
                    //取消shutdownHook
                    if (shutdownHookThread != null) {
                        try {
                            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                        } catch (IllegalStateException ignored) {
                        }
                    }
                    //标记不可用
                    available = false;

                }
            }
        }
    }

    protected String id(ConsumerRecord<String, byte[]> consumerRecord) {
        return consumerRecord.key();
    }

    /**
     * 消费
     */
    private void consume(KafkaConsumer<String, byte[]> consumer) {
        try {
            while (running_consume) {
                try {
                    //检查阻塞
                    if (maxBlockingNum > 0) {
                        if (blockingNum.sum() >= maxBlockingNum) {
                            TimeUnit.MILLISECONDS.sleep(100);
                            continue;
                        }
                    }
                    //消费一批数据
                    final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(3));
                    if (consumerRecords == null || consumerRecords.isEmpty()) {
                        continue;
                    }

                    //统计
                    final int count = consumerRecords.count();
                    blockingNum.add(count);
                    if (monitor_period > 0) {
                        monitor_consumeCount.add(count);
                    }

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
                        final String id = id(consumerRecord);
                        WorkExecutor workExecutor = getWorkExecutor(id);
                        //交给执行器处理
                        workExecutor.execute(() -> {
                            WorkHandler workHandler = workExecutor.workHandlers.computeIfAbsent(id, k -> {
                                WorkHandler temp = newHandler(id, workExecutor);
                                temp.init();
                                if (monitor_period > 0) {
                                    monitor_workHandlerCount.increment();
                                }
                                return temp;
                            });
                            workHandler.onMessage(consumerRecord);
                            if (monitor_period > 0) {
                                monitor_workCount.increment();
                            }
                            if (autoReleaseBlocking) {
                                blockingNum.decrement();
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error,Try Again After 3s", ex);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        throw BaseException.get(e);
                    }
                }
            }
        } finally {
            String assignment = consumer.assignment().stream().map(e -> e.topic() + ":" + e.partition()).collect(Collectors.joining(","));
            logger.info("consumer[{}] assignment[{}] close", this.getClass().getName(), assignment);
            consumer.close();
        }

    }

    private String getQueueLog(WorkExecutor executor) {
        return executor.executor.getActiveCount()
                + (workExecutorQueueSize > 0 ? ("/" + workExecutorQueueSize) : "")
                + ","
                + executor.blockingExecutor.getActiveCount();
    }

    /**
     * 监控日志
     * 如果需要修改日志、可以重写此方法
     */
    public String monitor_log() {
        return StringUtil.format("name[{}] workExecutor[{}] workHandler[{}] " +
                        "blocking[{}/{}] " +
                        "consume[{}/s] " +
                        "queues[{}] " +
                        "work[{}/s]",
                name, workExecutors.length, monitor_workHandlerCount.sum(),
                blockingNum.sum(), maxBlockingNum,
                monitor_consumeCount.sumThenReset() / monitor_period,
                Arrays.stream(workExecutors).map(this::getQueueLog).collect(Collectors.joining(" ")),
                monitor_workCount.sumThenReset() / monitor_period);
    }
}

