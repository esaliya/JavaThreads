package org.saliya.javathreads.damds;

import mpi.MPIException;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMLRT extends MM {
    public static void mmLoopGlobalData(double[][][] threadPartialBofZ, double[] preX,
                                        double[][] threadPartialMM) throws MPIException {

        /* Start main mmLoopLocalData*/
        if (ParallelOps.threadCount > 1){
            timer.start();
            compTimer.start();
            launchHabaneroApp(
                    () -> forallChunked(
                            0, ParallelOps.threadCount - 1,
                            (threadIdx) -> {
                                MMWorker mmWorker = new MMWorker
                                        (threadIdx, threadPartialBofZ[threadIdx], preX,
                                                threadPartialMM[threadIdx],
                                                globalColCount, targetDimension,
                                                blockSize);
                                for (int itr = 0; itr < iterations; ++itr) {
                                    mmWorker.run();
                                }
                            }));
            compTimer.stop();
            timer.stop();
        } else {
            timer.start();
            compTimer.start();
            MMWorker mmWorker = new MMWorker
                    (0, threadPartialBofZ[0], preX,
                            threadPartialMM[0],
                            globalColCount, targetDimension,
                            blockSize);
            for (int itr = 0; itr < iterations; ++itr) {
                mmWorker.run();
            }
            compTimer.stop();
            timer.stop();
        }

        time = timer.elapsed(TimeUnit.MILLISECONDS);
        sumTime += time;

        compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
        sumCompTime +=compTime;

        timer.reset();
        compTimer.reset();

        MMUtils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms");
    }
    public static void mmLoopLocalData(int[] threadRowCounts) throws MPIException {
        /* Start main mmLoopLocalData*/
        if (ParallelOps.threadCount > 1){
            launchHabaneroApp(
                    () -> forallChunked(
                            0, ParallelOps.threadCount - 1,
                            (threadIdx) -> {
                                MMWorker mmWorker = new MMWorker(threadIdx, globalColCount, targetDimension, blockSize, threadRowCounts[threadIdx]);
                                if (threadIdx == 0) {
                                    compTimer.start();
                                }
                                for (int itr = 0; itr < iterations; ++itr) {
                                    mmWorker.run();
                                }
                                if (threadIdx == 0) {
                                    compTimer.stop();
                                }
                            }));
        } else {
            MMWorker mmWorker = new MMWorker(0, globalColCount, targetDimension, blockSize, threadRowCounts[0]);
            compTimer.start();
            for (int itr = 0; itr < iterations; ++itr) {
                mmWorker.run();
            }
            compTimer.stop();
        }

        compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
        sumCompTime +=compTime;

        compTimer.reset();

        MMUtils.printMessage("Compute " +
                sumCompTime + " ms");
    }
}
