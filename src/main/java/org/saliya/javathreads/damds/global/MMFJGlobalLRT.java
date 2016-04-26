package org.saliya.javathreads.damds.global;

import mpi.MPIException;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.stream.IntStream;

public class MMFJGlobalLRT extends MMLRT{
    static double [][][] threadPartialBofZ;
    static double [] preX;
    static double [][] threadPartialMM;

    public static void main(String[] args)
        throws MPIException, InterruptedException, IOException {
        setup(args);
        MMUtils.printMessage("Running in Global Data LRT Mode");

        /* Allocate global arrays */
        allocateArrays(globalColCount);
        /* To keep things simple let's take data initialization out of the mmLoopLocalData*/
        initializeData(globalColCount);

        ParallelOps.worldProcsComm.barrier();
        mmLoopGlobalData(threadPartialBofZ, preX, threadPartialMM);
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
