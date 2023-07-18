package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtil {

    public static void sleep_ms(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void shutdownThenAwaitOneByOneAfterQueueEmpty(Queue<?> queue, ExecutorService executorService) {
        if (queue != null) {
            while (!queue.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }
        ExecutorUtil.shutdownThenAwaitOneByOne(executorService);
    }


    /**
     * 一个一个关闭等待
     *
     * @param executorServices
     */
    public static void shutdownThenAwaitOneByOne(ExecutorService... executorServices) {
        for (ExecutorService executorService : executorServices) {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    while (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {

                    }
                } catch (InterruptedException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            }
        }
    }

    /**
     * 先关闭所有、然后依次等待
     *
     * @param executorServices
     */
    public static void shutdownAllThenAwaitAll(ExecutorService... executorServices) {
        for (ExecutorService executorService : executorServices) {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
        for (ExecutorService executorService : executorServices) {
            if (executorService != null) {
                try {
                    while (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {

                    }
                } catch (InterruptedException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            }
        }
    }
}
