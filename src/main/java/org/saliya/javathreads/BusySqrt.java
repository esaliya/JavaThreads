package org.saliya.javathreads;

import net.openhft.affinity.Affinity;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.concurrent.CountDownLatch;

public class BusySqrt implements Runnable{
    private final double[] results;
    private final int threadIdx;
    private final boolean bind;
    private final CountDownLatch latch;
    private final int cores;

    public BusySqrt(
        double[] results, int threadIdx, boolean bind, CountDownLatch latch, int cores) {
        this.results = results;
        this.threadIdx = threadIdx;
        this.bind = bind;
        this.latch = latch;
        this.cores = cores;
    }

    @Override
    public void run() {

        BitSet bitSet = new BitSet(cores);
        bitSet.set(threadIdx);
        Affinity.setAffinity(bitSet);
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
