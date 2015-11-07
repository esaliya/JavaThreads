package org.saliya.javathreads;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class BusySqrt implements Runnable{
    private final double[] results;
    private final int threadIdx;
    private final boolean bind;
    private final CountDownLatch latch;

    public BusySqrt(
        double[] results, int threadIdx, boolean bind, CountDownLatch latch) {
        this.results = results;
        this.threadIdx = threadIdx;
        this.bind = bind;
        this.latch = latch;
    }

    @Override
    public void run() {
        compute();
    }

    private void compute() {
        double x = Math.random()*1e10;
        for (int i = 0; i < 1000000000; ++i){
            x = Math.sqrt(x*Math.random()*1.e10);
        }
        results[threadIdx] = x;
        latch.countDown();
    }
}
