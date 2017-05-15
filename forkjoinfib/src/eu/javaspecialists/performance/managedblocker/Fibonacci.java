package eu.javaspecialists.performance.managedblocker;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

// demo1: test100_000_000() time = 46547
// demo2: test100_000_000() time = 22470
// demo3: test100_000_000() time = 14188
// demo4: test100_000_000() time = 10356
// demo5: test100_000_000() time = 7073
// demo6: test100_000_000() time = 6908

// TODO: Would you like to subscribe to my free advanced Java newsletter?
// TODO: Or are you subscribed already and would like to say "hi"?
// TODO: Do both here: tinyurl.com/devoxxuk17
public class Fibonacci {
    private final BigInteger RESERVED = BigInteger.valueOf(-1000);

    public BigInteger f(int n) {
        Map<Integer, BigInteger> cache = new ConcurrentHashMap<>();
        cache.put(0, BigInteger.ZERO);
        cache.put(1, BigInteger.ONE);
        return f(n, cache);
    }

    private BigInteger f(int n, Map<Integer, BigInteger> cache) {
        BigInteger result = cache.putIfAbsent(n, RESERVED);
        if (result == null) {
            int half = (n + 1) / 2;

            RecursiveTask<BigInteger> f0_task = new RecursiveTask<BigInteger>() {
                protected BigInteger compute() {
                    return f(half - 1, cache);
                }
            };
            f0_task.fork();
            BigInteger f1 = f(half, cache);
            BigInteger f0 = f0_task.join();

            long time = n > 10_000 ? System.currentTimeMillis() : 0;
            try {
                if (n % 2 == 1) {
                    result = f0.multiply(f0).add(f1.multiply(f1));
                } else {
                    result = f0.shiftLeft(1).add(f1).multiply(f1);
                }
                synchronized (RESERVED) {
                    cache.put(n, result);
                    RESERVED.notifyAll();
                }
            } finally {
                time = n > 10_000 ? System.currentTimeMillis() - time : 0;
                if (time > 50) {
                    System.out.printf("f(%d) took %d ms%n", n, time);
                }
            }
        } else if (result == RESERVED) {
            try {
                ReservedBlocker blocker = new ReservedBlocker(n, cache);
                ForkJoinPool.managedBlock(blocker);
                result = blocker.result;
            } catch (InterruptedException e) {
                throw new CancellationException("interrupted");
            }
        }
        return result;
    }
    private class ReservedBlocker implements ForkJoinPool.ManagedBlocker {
        private BigInteger result;
        private final int n;
        private final Map<Integer, BigInteger> cache;

        public ReservedBlocker(int n, Map<Integer, BigInteger> cache) {
            this.n = n;
            this.cache = cache;
        }

        public boolean isReleasable() {
            return (result = cache.get(n)) != RESERVED;
        }

        public boolean block() throws InterruptedException {
            synchronized (RESERVED) {
                while (!isReleasable()) {
                    RESERVED.wait();
                }
            }
            return true;
        }
    }
}
