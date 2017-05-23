package eu.javaspecialists.performance.managedblocker;

import java.math.*;
import java.util.concurrent.*;

// demo1: test100_000_000() time = 45958
// 21572
// 10000 +/-

// TODO: Sign up to Heinz's "Java Specialists' Newsletter":
// TODO: tinyurl.com/ctjc2017
// TODO: (Already signed up?  Say "hi" on same link)
public class Fibonacci {
    public BigInteger f(int n) {
        if (n == 0) return BigInteger.ZERO;
        if (n == 1) return BigInteger.ONE;

        int half = (n + 1) / 2;

        RecursiveTask<BigInteger> f0_task = new RecursiveTask<BigInteger>() {
            protected BigInteger compute() {
                return f(half - 1);
            }
        };
        f0_task.fork();
        BigInteger f1 = f(half);
        BigInteger f0 = f0_task.join();

        if (n % 2 == 1) {
            return f0.multiply(f0).add(f1.multiply(f1));
        } else {
            return f0.shiftLeft(1).add(f1).multiply(f1);
        }
    }
}
