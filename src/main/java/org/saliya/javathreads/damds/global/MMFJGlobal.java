package org.saliya.javathreads.damds.global;

import mpi.MPIException;
import org.saliya.javathreads.damds.MMFJ;
import org.saliya.javathreads.damds.MMUtils;
import org.saliya.javathreads.damds.MMWorker;
import org.saliya.javathreads.damds.ParallelOps;

import java.io.IOException;
import java.util.stream.IntStream;

public class MMFJGlobal extends MMFJ{
    static double [][][] threadPartialBofZ;
    static double [] preX;
    static double [][] threadPartialMM;

    public static void main(String[] args)
        throws MPIException, InterruptedException, IOException {
        setup(args);

        /* Allocate global arrays */
        allocateArrays(globalColCount);
        /* To keep things simple let's take data initialization out of the mmLoop*/
        initializeData(globalColCount);

        MMWorker[] workers = new MMWorker[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> workers[i] =
                new MMWorker(i, threadPartialBofZ[i], preX, threadPartialMM[i],
                        globalColCount, targetDimension, blockSize));

        ParallelOps.worldProcsComm.barrier();
        mmLoop(workers);
        ParallelOps.tearDownParallelism();
    }

    private static void allocateArrays(int globalColCount) {
        preX = new double[globalColCount*targetDimension];
        threadPartialMM = new double[ParallelOps.threadCount][];
        threadPartialBofZ = new double[ParallelOps.threadCount][][];
        int threadRowCount;
        for (int i = 0; i < ParallelOps.threadCount; ++i){
            threadRowCount = ParallelOps.threadRowCounts[i];
            threadPartialBofZ[i] = new double[threadRowCount][ParallelOps.globalColCount];
            threadPartialMM[i] = new double[threadRowCount * targetDimension];
        }
    }

    private static void initializeData(int globalColCount) throws MPIException {
        /* Generate initial mapping of points */
        MMUtils.generatePreX(globalColCount,
                targetDimension, preX);

        /* Set numbers for BofZ*/
        IntStream.range(0, ParallelOps.threadCount).forEach(
                t -> MMUtils.generateBofZ(t,
                        globalColCount, threadPartialBofZ[t]));
    }
}
