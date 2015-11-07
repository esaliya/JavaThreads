package org.saliya.javathreads;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class AffinityThreads {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = Integer.parseInt(args[0]);
        final double[] results = new double[numThreads];
        final Thread[] threads = new Thread[numThreads];
        final CountDownLatch latch = new CountDownLatch(numThreads);
        for (int i = 0; i < numThreads; ++i){
            threads[i] = new Thread(new BusySqrt(results, i, false, latch));
            threads[i].start();
        }
        latch.await();

        System.out.println(Arrays.toString(results));
    }
}
