package org.saliya.javathreads.damds;

import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public abstract class MMFJ extends MM {
    public static void mmLoopGlobalData(double[][][] threadPartialBofZ, double[] preX,
                                        double[][] threadPartialMM) throws MPIException {
        /* Start main mmLoop*/
        for (int itr = 0; itr < iterations; ++itr) {
            timer.start();
            if (ParallelOps.threadCount > 1){
                compTimer.start();
                launchHabaneroApp(
                        () -> forallChunked(
                                0, ParallelOps.threadCount - 1,
                                (threadIdx) -> {
                                    MatrixUtils
                                            .matrixMultiply(threadPartialBofZ[threadIdx], preX, ParallelOps.threadRowCounts[threadIdx],
                                                    targetDimension, globalColCount, blockSize, threadPartialMM[threadIdx]);
                                }));
                compTimer.stop();
            } else {
                compTimer.start();
                MatrixUtils
                        .matrixMultiply(threadPartialBofZ[0], preX, ParallelOps.threadRowCounts[0],
                                targetDimension, globalColCount, blockSize, threadPartialMM[0]);
                compTimer.stop();
            }

            timer.stop();

            time = timer.elapsed(TimeUnit.MILLISECONDS);
            sumTime += time;

            compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
            sumCompTime +=compTime;

            timer.reset();
            compTimer.reset();
        }
        MMUtils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms");
    }
    public static void mmLoopLocalData(MMWorker[] workers) throws MPIException {
        /* Start main mmLoop*/
        for (int itr = 0; itr < iterations; ++itr) {
            timer.start();
            if (ParallelOps.threadCount > 1){
                compTimer.start();
                launchHabaneroApp(
                        () -> forallChunked(
                                0, ParallelOps.threadCount - 1,
                                (threadIdx) -> {
                                   workers[threadIdx].run();
                                }));
                compTimer.stop();
            } else {
                compTimer.start();
                workers[0].run();
                compTimer.stop();
            }

            timer.stop();

            time = timer.elapsed(TimeUnit.MILLISECONDS);
            sumTime += time;

            compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
            sumCompTime +=compTime;

            timer.reset();
            compTimer.reset();
        }
        MMUtils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms");
    }
}
