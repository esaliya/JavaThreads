package org.saliya.javathreads.damds.local;

import net.openhft.lang.io.Bytes;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadCommunicator {
    private int threadCount;
    private int[] intBuffer;
    private double[] doubleBuffer;
    private double[] pointsBuffer;
    private CyclicBarrier barrier;
    private Lock lock = new ReentrantLock();
    private double sum = 0;
    private AtomicInteger sumCount = new AtomicInteger(0);
    private AtomicInteger sumCount2 = new AtomicInteger(0);
    private AtomicInteger sumCountDoubleStats = new AtomicInteger(0);
    private AtomicInteger collectCounter = new AtomicInteger(0);
    private AtomicInteger copyCounter = new AtomicInteger(0);
    private AtomicInteger bcastCounter = new AtomicInteger(0);
    private AtomicInteger bcastCounterStats = new AtomicInteger(0);
    private AtomicInteger barrierCounter = new AtomicInteger(0);

    public ThreadCommunicator(int threadCount, int numberDataPoints, int targetDimension) {
        this.threadCount = threadCount;
        intBuffer = new int[threadCount];
        doubleBuffer = new double[threadCount];
        pointsBuffer = new double[numberDataPoints*targetDimension];
        barrier = new CyclicBarrier(threadCount);
    }

    /**
     * Sum value over threads and collects at root
     * @param threadIdx the thread index
     * @param val the value
     * @return the summation
     * @throws BrokenBarrierException
     * @throws InterruptedException
     */
    public void sumIntOverThreads(int threadIdx, RefObj<Integer> val)
        throws BrokenBarrierException, InterruptedException {
        intBuffer[threadIdx] = val.getValue();
        barrier.await();
        int sum = 0;
        for (int i = 0; i < threadCount; ++i){
            sum += intBuffer[i];
        }
        val.setValue(sum);
    }

    public void sumDoublesOverThreads(int threadIdx, RefObj<Double> val)
        throws BrokenBarrierException, InterruptedException {
        sumCount.compareAndSet(threadCount, 0);

        doubleBuffer[threadIdx] = val.getValue();
        sumCount.getAndIncrement();
        // thread 0 waits for others to update
        if (threadIdx == 0) {
            while (sumCount.get() != threadCount) {
                //System.out.println("l1");
            }
            double sum = 0.0;
            for (int i = 0; i < threadCount; ++i){
                sum += doubleBuffer[i];
            }
            val.setValue(sum);
        }
    }

    public void bcastIntOverThreads(int threadIdx, RefObj<Integer> val, int root)
        throws BrokenBarrierException, InterruptedException {
        if (threadIdx == root){
            final Integer value = val.getValue();
            for (int i = 0; i < threadCount; ++i){
                intBuffer[i] = value;
            }
        }
        barrier.await();
        val.setValue(intBuffer[threadIdx]);
    }

    public void bcastDoubleOverThreads(int threadIdx, RefObj<Double> val, int root)
        throws BrokenBarrierException, InterruptedException {
        if (threadIdx == root){
            final double value = val.getValue();
            for (int i = 0; i < threadCount; ++i){
                doubleBuffer[i] = value;
            }
//            System.out.println("Set sum count");
            sumCount2.set(threadCount);
        }

        while (sumCount2.get() == 0) {
            // System.out.println("l2");
        }
        sumCount2.decrementAndGet();
//        System.out.println("l2");
//        barrier.await();
        val.setValue(doubleBuffer[threadIdx]);
    }

    public void barrier()
        throws BrokenBarrierException, InterruptedException {
        barrierCounter.compareAndSet(threadCount, 0);
        barrierCounter.incrementAndGet();
        while (barrierCounter.get() != threadCount) {
            ;
        }
//        barrier.await();
    }


    public void bcastDoubleArrayOverThreads(int threadIdx, double[] preX, int root)
        throws BrokenBarrierException, InterruptedException {
        bcastCounter.compareAndSet(threadCount, 0);
        if (threadIdx == root){
            System.arraycopy(preX, 0, pointsBuffer, 0, preX.length);
        }
//        barrier.await();
        bcastCounter.getAndIncrement();
        while (bcastCounter.get() != threadCount) {
            ;
        }
        System.arraycopy(pointsBuffer, 0, preX, 0, pointsBuffer.length);
    }

    public synchronized void collect(
        int startIndex, double[] val, Bytes bytes) {
        int pos = startIndex;
        for (double aVal : val) {
            bytes.position(pos);
            bytes.writeDouble(aVal);
            pos+=Double.BYTES;
        }
    }

    public synchronized void copy(Bytes from, double[] to, int count) {
        from.position(0);
        for (int i = 0; i < count; ++i){
            to[i] = from.readDouble();
        }

    }

    public void collect2(
            int startIndex, double[] val, Bytes bytes, int threadid) {
        //System.out.println("col " + threadid);
        collectCounter.compareAndSet(threadCount, 0);
        int pos = startIndex;
//        lock.lock();
        for (double aVal : val) {
            bytes.position(pos);
            bytes.writeDouble(aVal);
            pos+=Double.BYTES;
        }
//        lock.unlock();
        collectCounter.getAndIncrement();
        while (collectCounter.get() != threadCount) {
//            System.out.println(collectCounter.get());
            ;
        }
        //System.out.println("Col");
    }

    public void copy2(Bytes from, double[] to, int count, int threadId) {
        copyCounter.compareAndSet(threadCount, 0);
        //System.out.println("Copy " + threadId);
//        lock.lock();
        from.position(0);
        for (int i = 0; i < count; ++i){
            to[i] = from.readDouble();
        }
//        lock.unlock();
        copyCounter.getAndIncrement();
        while (copyCounter.get() != threadCount) {
            ;
        }
        //System.out.println("Copy");
    }
}
