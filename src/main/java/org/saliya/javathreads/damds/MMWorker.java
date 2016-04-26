package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import org.saliya.javathreads.MatrixUtils;

public class MMWorker {
    int threadIdx;
    double[][] partialBofZ;
    double[] preX;
    double[] partialMM;

    int globalColCount;
    int targetDimension;
    int blockSize;

    Stopwatch compInternalTimer;

    public MMWorker(int threadIdx, double[][] partialBofZ, double[] preX,
                    double[] partialMM,
                    int globalColCount, int targetDimension, int blockSize) {
        this.threadIdx = threadIdx;
        this.partialBofZ = partialBofZ;
        this.preX = preX;
        this.partialMM = partialMM;
        this.globalColCount = globalColCount;
        this.targetDimension = targetDimension;
        this.blockSize = blockSize;
    }

    public MMWorker(int threadIdx, int globalColCount, int targetDimension, int blockSize){
        this.threadIdx = threadIdx;
        this.globalColCount = globalColCount;
        this.targetDimension = targetDimension;
        this.blockSize = blockSize;

        /* Allocate arrays */
        allocateArrays();

        /* To keep things simple let's take data initialization out of the mmLoop*/
        initializeData();
    }

    private void initializeData() {
        /* Generate initial mapping of points */
        MMUtils.generatePreX(globalColCount,
                targetDimension, preX);

        /* Set numbers for BofZ*/
        MMUtils.generateBofZ(threadIdx,
                globalColCount, partialBofZ);
    }

    private void allocateArrays() {
        preX = new double[globalColCount*targetDimension];
        partialMM = new double[ParallelOps.threadRowCounts[threadIdx] * targetDimension];
        partialBofZ = new double[ParallelOps.threadRowCounts[threadIdx]][ParallelOps.globalColCount];
    }

    public void run() {
        MatrixUtils
                .matrixMultiply(partialBofZ, preX, ParallelOps.threadRowCounts[threadIdx],
                        targetDimension, globalColCount, blockSize, partialMM);
    }
}
