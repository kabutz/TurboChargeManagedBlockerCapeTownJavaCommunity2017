package eu.javaspecialists.performance.managedblocker;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

// demo1: test100_000_000() time = 45958
// 21572
// 10000 +/-
// demo2: test100_000_000() time = 22145
// demo3: test100_000_000() time = 14743
// demo4: test100_000_000() time = 10141


// TODO: Sign up to Heinz's "Java Specialists' Newsletter":
// TODO: tinyurl.com/ctjc2017
// TODO: (Already signed up?  Say "hi" on same link)
public class Fibonacci {
    public BigInteger f(int n) {
        Map<Integer, BigInteger> cache = new ConcurrentHashMap<>();
        cache.put(0, BigInteger.ZERO);
        cache.put(1, BigInteger.ONE);
        return f(n, cache);
    }

    public BigInteger f(int n, Map<Integer, BigInteger> cache) {
        BigInteger result = cache.get(n);
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
                    result =  f0.multiply(f0).add(f1.multiply(f1));
                } else {
                    result = f0.shiftLeft(1).add(f1).multiply(f1);
                }
                cache.put(n, result);
            } finally {
                time = n > 10_000 ? System.currentTimeMillis() - time : 0;
                if (time > 50) {
                    System.out.printf("f(%d) took %d ms%n", n, time);
                }
            }
        }
        return result;
    }
}
