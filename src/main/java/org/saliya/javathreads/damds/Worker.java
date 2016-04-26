package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.stream.IntStream;

public class Worker {
    int threadIdx;
    double[][] partialBofZ;
    double[] preX;
    double[] partialMM;

    int globalColCount;
    int targetDimension;
    int blockSize;

    Stopwatch compInternalTimer;

    public Worker(int threadIdx, double[][] partialBofZ, double[] preX, double[] partialMM,
                  int globalColCount, int targetDimension, int blockSize) {
        this.threadIdx = threadIdx;
        this.partialBofZ = partialBofZ;
        this.preX = preX;
        this.partialMM = partialMM;
        this.globalColCount = globalColCount;
        this.targetDimension = targetDimension;
        this.blockSize = blockSize;
    }

    public void run() {
        MatrixUtils
                .matrixMultiply(partialBofZ, preX, ParallelOps.threadRowCounts[threadIdx],
                        targetDimension, globalColCount, blockSize, partialMM);
    }
}
