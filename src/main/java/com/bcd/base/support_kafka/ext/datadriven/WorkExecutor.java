package com.bcd.base.support_kafka.ext.datadriven;

import com.bcd.base.exception.BaseException;
import com.bcd.base.util.DateUtil;
import com.bcd.base.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 工作执行器
 * 通过两个线程执行不同的任务
 * <p>
 * - {@link #executor}执行非阻塞任务
 * 调用如下方法
 * {@link #execute(Runnable)}
 * {@link #submit(Supplier)}
 * <p>
 * 注意:
 * 非阻塞任务线程中的任务不能阻塞、且任务之间是串行执行的、没有线程安全问题
 */
public class WorkExecutor {

    static Logger logger = LoggerFactory.getLogger(WorkExecutor.class);

    public final String threadName;

    public final int queueSize;

    public final BlockingChecker blockingChecker;

    /**
     * 任务执行器的任务队列
     */
    public final BlockingQueue<Runnable> blockingQueue;

    /**
     * 任务执行器
     */
    public ThreadPoolExecutor executor;

    /**
     * 阻塞检查器
     */
    public ScheduledThreadPoolExecutor executor_blockingChecker;

    /**
     * 存储本执行器所有的handler
     */
    public final Map<String, WorkHandler> workHandlers = new HashMap<>();



    public static final class BlockingChecker {
        public final int periodInSecond;
        public final int expiredInSecond;

        public BlockingChecker(int periodInSecond, int expiredInSecond) {
            this.periodInSecond = periodInSecond;
            this.expiredInSecond = expiredInSecond;
        }

        /**
         * @param periodInSecond  定时任务周期(秒)
         * @param expiredInSecond 判断阻塞时间(秒)
         */
        public static BlockingChecker get(int periodInSecond, int expiredInSecond) {
            return new BlockingChecker(periodInSecond, expiredInSecond);
        }
    }

    /**
     * 构造任务执行器
     *
     * @param threadName
     * @param queueSize       无阻塞任务线程池队列大小
     *                        0则使用{@link LinkedBlockingQueue}
     *                        否则使用{@link ArrayBlockingQueue}
     * @param blockingChecker 阻塞检查周期任务的执行周期(秒)
     *                        如果<=0则不启动阻塞检查
     *                        开启后会启动周期任务
     *                        检查逻辑为
     *                        向执行器中提交一个空任务、等待{@link BlockingChecker#expiredInSecond}秒后检查任务是否完成、如果没有完成则警告、且此后每一秒检查一次任务情况并警告
     */
    public WorkExecutor(String threadName, int queueSize, BlockingChecker blockingChecker) {
        this.threadName = threadName;
        this.queueSize = queueSize;
        this.blockingChecker = blockingChecker;
        if (queueSize <= 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else {
            blockingQueue = new ArrayBlockingQueue<>(queueSize);
        }
    }

    public final CompletableFuture<Void> execute(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public final <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public void init() {
        this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, blockingQueue, r -> new Thread(r, threadName),
                (r, executor) -> {
                    if (!executor.isShutdown()) {
                        try {
//                    logger.warn("workThread[{}] RejectedExecutionHandler",threadName);
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });

        if (blockingChecker != null) {
            //开启阻塞监控
            int expiredInSecond = blockingChecker.expiredInSecond;
            int periodInSecond = blockingChecker.periodInSecond;
            this.executor_blockingChecker = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, threadName + "-blockingChecker"));
            this.executor_blockingChecker.scheduleWithFixedDelay(() -> {
                long expired = DateUtil.CacheMillisecond.current() + expiredInSecond;
                CompletableFuture<Void> future = execute(() -> {
                });
                try {
                    TimeUnit.SECONDS.sleep(expiredInSecond);
                    while (!future.isDone()) {
                        long blockingSecond = DateUtil.CacheMillisecond.current() - expired;
                        if (blockingSecond >= 0) {
                            logger.warn("WorkExecutor blocking too long threadName[{}] blockingTime[{}s>={}s] queueSize[{}]", threadName, blockingSecond + expiredInSecond, expiredInSecond, executor.getQueue().size());
                        }
                        TimeUnit.SECONDS.sleep(1);
                    }
                } catch (InterruptedException ex) {
                    throw BaseException.get(ex);
                }
            }, periodInSecond, periodInSecond, TimeUnit.SECONDS);
        }
    }

    public void destroy() {
        ExecutorUtil.shutdownAllThenAwait(executor, executor_blockingChecker);
    }
}
