package com.bcd.base.support_kafka.ext.performance;

import com.bcd.base.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class WorkExecutor {

    static Logger logger= LoggerFactory.getLogger(WorkExecutor.class);

    public final ThreadPoolExecutor executor;

    public WorkExecutor(int queueSize, String threadName) {
        this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize)
                , r -> new Thread(r, threadName), (r, executor) -> {
            if (!executor.isShutdown()) {
                try {
//                    logger.warn("workThread[{}] RejectedExecutionHandler",threadName);
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    public void destroy() {
        ExecutorUtil.shutdown(executor);
    }
}
