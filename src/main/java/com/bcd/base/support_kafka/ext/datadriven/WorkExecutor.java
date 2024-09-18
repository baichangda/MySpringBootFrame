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

    public final ThreadPoolExecutor executor;

    public final ScheduledThreadPoolExecutor executor_blockingMonitor;

    /**
     * 存储本执行器所有的handler
     */
    final Map<String, WorkHandler> workHandlers = new HashMap<>();

    /**
     * 最后执行时间(秒)
     */
    volatile long lastTime;

    /**
     * 阻塞报警时间(秒)
     */
    private final static int expiredSecond = 5;

    /**
     * 构造任务执行器
     *
     * @param threadName
     * @param queueSize             无阻塞任务线程池队列大小
     *                              0则使用{@link LinkedBlockingQueue}
     *                              否则使用{@link ArrayBlockingQueue}
     * @param blockingMonitorPeriod 阻塞监控周期任务的执行周期(秒)
     *                              如果<=0则不启动阻塞检查
     *                              开启后会启动周期任务
     *                              检查逻辑为
     *                              向执行器中提交一个空任务、等待{@link #expiredSecond}秒后检查任务是否完成、如果没有完成则警告、且此后每一秒检查一次任务情况并警告
     */
    public WorkExecutor(String threadName, int queueSize, int blockingMonitorPeriod) {
        BlockingQueue<Runnable> blockingQueue;
        if (queueSize <= 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else {
            blockingQueue = new ArrayBlockingQueue<>(queueSize);
        }
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

        if (blockingMonitorPeriod > 0) {
            //开启阻塞监控
            this.executor_blockingMonitor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, threadName + "-blockingMonitor"));
            this.executor_blockingMonitor.scheduleWithFixedDelay(() -> {
                long expired = DateUtil.CacheMillisecond.current() + expiredSecond;
                CompletableFuture<Void> future = execute(() -> {
                });
                try {
                    TimeUnit.SECONDS.sleep(expiredSecond);
                    while (!future.isDone()) {
                        long blockingSecond = DateUtil.CacheMillisecond.current() - expired;
                        if (blockingSecond >= 0) {
                            logger.warn("WorkExecutor blocking too long threadName[{}] blockingTime[{}s>={}s] queueSize[{}]", threadName, blockingSecond + expiredSecond, expiredSecond, executor.getQueue().size());
                        }
                        TimeUnit.SECONDS.sleep(1);
                    }
                } catch (InterruptedException ex) {
                    throw BaseException.get(ex);
                }
            }, blockingMonitorPeriod, blockingMonitorPeriod, TimeUnit.SECONDS);
        } else {
            this.executor_blockingMonitor = null;
        }

    }

    public final CompletableFuture<Void> execute(Runnable runnable) {
        lastTime = DateUtil.CacheSecond.current();
        return CompletableFuture.runAsync(runnable, executor);
    }

    public final <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        lastTime = DateUtil.CacheSecond.current();
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public void destroy() {
        ExecutorUtil.shutdownAllThenAwait(executor, executor_blockingMonitor);
    }
}
