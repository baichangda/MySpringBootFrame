
package com.bcd.sys.task;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public interface ExecutorChooser {

    ThreadPoolExecutor next();

    static ExecutorChooser getChooser(ThreadPoolExecutor[] pools) {
        if (isPowerOfTwo(pools.length)) {
            return new PowerOfTwoEventExecutorChooser(pools);
        } else {
            return new GenericEventExecutorChooser(pools);
        }
    }

    static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    final class PowerOfTwoEventExecutorChooser implements ExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final ThreadPoolExecutor[] pools;

        public PowerOfTwoEventExecutorChooser(ThreadPoolExecutor[] pools) {
            this.pools = pools;
        }

        @Override
        public ThreadPoolExecutor next() {
            return pools[idx.getAndIncrement() & pools.length - 1];
        }
    }

    final class GenericEventExecutorChooser implements ExecutorChooser {
        // Use a 'long' counter to avoid non-round-robin behaviour at the 32-bit overflow boundary.
        // The 64-bit long solves this by placing the overflow so far into the future, that no system
        // will encounter this in practice.
        private final AtomicLong idx = new AtomicLong();
        private final ThreadPoolExecutor[] pools;

        public GenericEventExecutorChooser(ThreadPoolExecutor[] pools) {
            this.pools = pools;
        }

        @Override
        public ThreadPoolExecutor next() {
            return pools[(int) Math.abs(idx.getAndIncrement() % pools.length)];
        }
    }
}
