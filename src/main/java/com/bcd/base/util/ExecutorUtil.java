package com.bcd.base.util;


import com.bcd.base.exception.BaseException;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ExecutorUtil {

    /**
     * 循环从队列里面拿数据、堆积一批到缓存中、然后触发回调
     * 如下两种情况触发回调
     * 1、缓存数量到达期望数量、此时数量为except
     * 2、当队列数量为空、即将进入阻塞前一刻、此时数量<except
     *
     * @param queue    阻塞队列
     * @param except   期望数量
     * @param callback 回调方法
     * @param running  是否运行中、通过设置此为false打断循环
     * @param <T>
     */
    public static <T> void loop(BlockingQueue<T> queue, int except, Consumer<ArrayList<T>> callback, AtomicBoolean running) {
        try {
            final ArrayList<T> cache = new ArrayList<>(except);
            while (true) {
                T t = queue.poll(3, TimeUnit.SECONDS);
                if (t == null) {
                    if (running.get()) {
                        continue;
                    } else {
                        break;
                    }
                }
                do {
                    cache.add(t);
                    if (cache.size() == except) {
                        callback.accept(cache);
                        cache.clear();
                    }
                    t = queue.poll();
                } while (t != null);
                callback.accept(cache);
                cache.clear();
            }
        } catch (InterruptedException ex) {
            throw BaseException.get(ex);
        }
    }

    /**
     * 一个一个关闭等待结束
     * <p>
     * 支持{@link ExecutorService}、{@link ExecutorService[]}
     * - 关闭线程池
     * - 等待线程池执行完毕
     * <p>
     * 支持{@link Thread}、{@link Thread[]}
     * - 等待线程执行完毕
     * <p>
     * 支持{@link java.util.Queue}、{@link java.util.Queue[]}
     * - 等待队列为空
     *
     * @param args
     */
    public static void shutdownThenAwait(Object... args) {
        if (args == null || args.length == 0) {
            return;
        }
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof ExecutorService pool) {
                    pool.shutdown();
                    await(pool);
                } else if (arg instanceof ExecutorService[] pools) {
                    for (ExecutorService pool : pools) {
                        pool.shutdown();
                        await(pool);
                    }
                } else if (arg instanceof Thread thread) {
                    await(thread);
                } else if (arg instanceof Thread[] threads) {
                    await((Object) threads);
                } else if (arg instanceof BlockingQueue<?> queue) {
                    await(queue);
                } else if (arg instanceof BlockingQueue<?>[] queues) {
                    await((Object) queues);
                } else {
                    throw BaseException.get("arg type[{}] not support", arg.getClass().getName());
                }
            }
        }
    }

    /**
     * 先停止所有
     * 然后等待关闭
     * <p>
     * 支持{@link ExecutorService}、{@link ExecutorService[]}
     * - 关闭线程池
     * - 等待线程池执行完毕
     * <p>
     * 支持{@link Thread}、{@link Thread[]}
     * - 等待线程执行完毕
     * <p>
     * 支持{@link java.util.Queue}、{@link java.util.Queue[]}
     * - 等待队列为空
     *
     * @param args
     */
    public static void shutdownAllThenAwait(Object... args) {
        if (args == null || args.length == 0) {
            return;
        }
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof ExecutorService pool) {
                    pool.shutdown();
                } else if (arg instanceof ExecutorService[] pools) {
                    for (ExecutorService pool : pools) {
                        pool.shutdown();
                    }
                }
            }
        }

        for (Object arg : args) {
            await(arg);
        }

    }


    /**
     * 等待资源关闭
     * <p>
     * 支持{@link ExecutorService}、{@link ExecutorService[]}
     * - 等待线程池执行完毕
     * <p>
     * 支持{@link Thread}、{@link Thread[]}
     * - 等待线程执行完毕
     * <p>
     * 支持{@link java.util.Queue}、{@link java.util.Queue[]}
     * - 等待队列为空
     *
     * @param args
     */
    public static void await(Object... args) {
        if (args == null || args.length == 0) {
            return;
        }
        try {
            for (Object arg : args) {
                if (arg != null) {
                    if (arg instanceof ExecutorService pool) {
                        while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {

                        }
                    } else if (arg instanceof ExecutorService[] pools) {
                        for (ExecutorService pool : pools) {
                            while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {

                            }
                        }
                    } else if (arg instanceof Thread thread) {
                        thread.join();
                    } else if (arg instanceof Thread[] threads) {
                        for (Thread thread : threads) {
                            thread.join();
                        }
                    } else if (arg instanceof BlockingQueue<?> queue) {
                        while (!queue.isEmpty()) {
                            TimeUnit.MILLISECONDS.sleep(100L);
                        }
                    } else if (arg instanceof BlockingQueue<?>[] queues) {
                        for (BlockingQueue<?> queue : queues) {
                            while (!queue.isEmpty()) {
                                TimeUnit.MILLISECONDS.sleep(100L);
                            }
                        }
                    } else {
                        throw BaseException.get("arg type[{}] not support", arg.getClass().getName());
                    }
                }

            }
        } catch (InterruptedException e) {
            throw BaseException.get(e);
        }
    }

}
