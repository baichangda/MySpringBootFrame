package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
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

    public static void shutdown(Object... args) {
        if (args == null) {
            return;
        }
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof ExecutorService pool) {
                    shutdownThenAwait(pool);
                } else if (arg instanceof ExecutorService[] pools) {
                    shutdownThenAwait(pools);
                } else if (arg instanceof BlockingQueue<?> queue) {
                    awaitQueueEmpty(queue);
                } else if (arg instanceof BlockingQueue<?>[] queues) {
                    awaitQueueEmpty(queues);
                } else {
                    throw BaseRuntimeException.getException("arg type[{}] not support", arg.getClass().getName());
                }
            }
        }
    }


    /**
     * @param pools
     */
    public static void shutdownThenAwait(ExecutorService... pools) {
        if (pools == null) {
            return;
        }
        for (ExecutorService pool : pools) {
            pool.shutdown();
            try {
                while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {

                }
            } catch (InterruptedException ex) {
                throw BaseRuntimeException.getException(ex);
            }
        }
    }

    public static void awaitQueueEmpty(Queue<?>... queues) {
        if (queues == null) {
            return;
        }
        for (Queue<?> queue : queues) {
            while (!queue.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200L);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }
    }

}
