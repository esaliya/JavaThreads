package org.saliya.javathreads;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class AffinityThreads {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = Integer.parseInt(args[0]);
        int numCores = Integer.parseInt(args[1]);
        boolean bind = Boolean.parseBoolean(args[2]);
        boolean useHJ = Boolean.parseBoolean(args[3]);
        final double[] results = new double[numThreads];
        final Thread[] threads = new Thread[numThreads];
        final CountDownLatch latch = new CountDownLatch(numThreads);
        if (!useHJ) {
            System.out.println("Using " +numThreads + "/" + numCores + " Java Threads with binding = " + bind);
            for (int i = 0; i < numThreads; ++i) {
                threads[i] = new Thread(
                    new BusySqrt(results, i, bind, latch, numCores));
                threads[i].start();
            }
        } else {
            System.out.println("Using " +numThreads + "/" + numCores + " HJ with binding = " + bind);
            launchHabaneroApp(
                () -> forallChunked(
                    0, numThreads - 1,
                    (threadIdx) -> {
                        new BusySqrt(results, threadIdx, bind, latch, numCores).run();
                    }));
        }
        latch.await();

        System.out.println(Arrays.toString(results));
    }
}
