package org.saliya.javathreads;

import com.google.common.base.Stopwatch;
import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;
import net.openhft.affinity.Affinity;

import java.nio.DoubleBuffer;
import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class BCReplica {
    static double [][][] threadPartialBofZ;
    static double [][] preX;
    static double [][][] threadPartialOutMM;
    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch[] timers;

    public static void main(String[] args)
        throws MPIException, InterruptedException {
        args = MPI.Init(args);
        Intracomm worldProcComm = MPI.COMM_WORLD;
        int worldProcRank = worldProcComm.getRank();
        int worldProcCount = worldProcComm.getSize();

        int threadCount = Integer.parseInt(args[0]);
        int coreCount = Integer.parseInt(args[1]);
        boolean bind = Boolean.parseBoolean(args[2]);
        boolean useHJ = Boolean.parseBoolean(args[3]);
        int iterations = Integer.parseInt(args[4]);

        boolean lrt = args.length > 5 && Boolean.parseBoolean(args[5]);

        final int globalColCount = args.length > 6 ? Integer.parseInt(args[6]) : 50000;
        final int totalComputingUnits = args.length > 7 ? Integer.parseInt(args[7]) : 24 * 20;

        int rowCountPerUnit = globalColCount/ totalComputingUnits;

        preX = new double[globalColCount][targetDimension];
        threadPartialOutMM = new double[threadCount][rowCountPerUnit][targetDimension];
        threadPartialBofZ = new double[threadCount][rowCountPerUnit][globalColCount];
        timers = new Stopwatch[threadCount];

        final CountDownLatch[] lrtLatch = new CountDownLatch[1];
        lrtLatch[0] = new CountDownLatch(threadCount);
        AtomicBoolean workToDo = new AtomicBoolean(false);
        AtomicBoolean readyToGo = new AtomicBoolean(true);
        AtomicBoolean done = new AtomicBoolean(false);
        if (threadCount > 1 && lrt){
            if (!useHJ){
                for (int i = 0; i < threadCount; ++i){
                    final int threadIdx = i;
                    new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                while (!done.get()) {
                                    while (!workToDo.get()) {/*busy wait*/}
                                    if (done.get()){
                                        lrtLatch[0].countDown();
                                        return;
                                    }

                                    timers[threadIdx].start();
                                    MatrixUtils.matrixMultiply(
                                        threadPartialBofZ[threadIdx], preX,
                                        rowCountPerUnit, targetDimension,
                                        globalColCount, blockSize,
                                        threadPartialOutMM[threadIdx]);
                                    timers[threadIdx].stop();
                                    lrtLatch[0].countDown();

                                    while (!readyToGo.get()) {/*busy wait*/}
                                }
                            }
                        }).start();
                }
            }
        }


        DoubleBuffer buff = MPI.newDoubleBuffer(1);
        double avgTime = 0.0d;
        for (int itr = 0; itr < iterations; ++itr){
            long time = 0;
            IntStream.range(0, globalColCount).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                        preX[r][c] = Math.random();
                    }));
            IntStream.range(0,threadCount).forEach(t -> IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, globalColCount).forEach(c -> {
                            threadPartialBofZ[t][r][c] = Math.random();
                        })));
            IntStream.range(0,threadCount).forEach(t -> IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                            threadPartialOutMM[t][r][c] = 0.0;
                        })));
            IntStream.range(0,threadCount).forEach(t -> timers[t] = Stopwatch.createUnstarted());

            if (threadCount > 1){
                if (lrt){
                    if (!useHJ){
                        readyToGo.compareAndSet(true, false);
                        workToDo.compareAndSet(false, true);
                        lrtLatch[0].await();
                        lrtLatch[0] = new CountDownLatch(threadCount);
                        workToDo.compareAndSet(true, false);
                        readyToGo.compareAndSet(false, true);

                        if (worldProcRank == 0){
                            System.out.println("Done iteration " + itr);
                        }
                    }
                } else {
                    if (useHJ) {
                        launchHabaneroApp(
                            () -> forallChunked(
                                0, threadCount - 1, (threadIdx) -> {
                                    if (bind) {
                                        BitSet bitSet = new BitSet(coreCount);
                                        bitSet.set(threadIdx);
                                        Affinity.setAffinity(bitSet);
                                    }
                                    timers[threadIdx].start();
                                    MatrixUtils.matrixMultiply(
                                        threadPartialBofZ[threadIdx], preX,
                                        rowCountPerUnit, targetDimension,
                                        globalColCount, blockSize,
                                        threadPartialOutMM[threadIdx]);
                                    timers[threadIdx].stop();
                                }));
                    }
                    else {
                        final CountDownLatch latch = new CountDownLatch(threadCount);
                        final CountDownLatch go = new CountDownLatch(1);
                        for (int i = 0; i < threadCount; ++i) {
                            final int threadIdx = i;
                            new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            go.await();
                                            timers[threadIdx].start();
                                            MatrixUtils.matrixMultiply(
                                                threadPartialBofZ[threadIdx],
                                                preX, rowCountPerUnit,
                                                targetDimension, globalColCount,
                                                blockSize, threadPartialOutMM
                                                    [threadIdx]);
                                            timers[threadIdx].stop();
                                            latch.countDown();
                                        }
                                        catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                        }
                        go.countDown();
                        latch.await();

                    }
                }
            } else {
                timers[0].start();
                MatrixUtils.matrixMultiply(
                    threadPartialBofZ[0], preX,
                    rowCountPerUnit,
                    targetDimension, globalColCount,
                    blockSize, threadPartialOutMM[0]);
                timers[0].stop();
            }

            // This is common for both threaded and non thread case
            for (int t = 0; t < threadCount; ++t){
                time+= timers[t].elapsed(TimeUnit.MILLISECONDS);
                timers[t].reset();
            }
            avgTime += (time *1.0 / threadCount);
        }

        buff.put(0,avgTime);
        worldProcComm.reduce(buff, 1, MPI.DOUBLE, MPI.SUM, 0);
        if (worldProcRank == 0) {
            avgTime = buff.get(0);
            System.out.println(
                "Average Time " + (avgTime / (iterations * worldProcCount))
                + " " + "ms");
        }

        // Shutdown long running threads if any
        if (threadCount > 1 && lrt){
            if (!useHJ){
                done.compareAndSet(false, true);
                workToDo.compareAndSet(false, true);
                lrtLatch[0].await();
            }
        }

        MPI.Finalize();
    }
}
