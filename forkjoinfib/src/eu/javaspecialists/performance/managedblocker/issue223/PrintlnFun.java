package eu.javaspecialists.performance.managedblocker.issue223;

import java.util.stream.*;

// TODO: Would you like to subscribe to my free advanced Java newsletter?
// TODO: Or are you subscribed already and would like to say "hi"?
// TODO: Do both here: tinyurl.com/devoxxuk17
public class PrintlnFun {
    public static void main(String... args) {
        synchronized (System.out) {
            System.out.println("Hello World");
            IntStream.range(0, 4).parallel().
                forEach(System.out::println);
        }
    }
}
