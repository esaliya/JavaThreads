package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import org.saliya.javathreads.MatrixUtils;

import java.util.concurrent.TimeUnit;

public class MMWorker {
    int threadIdx;
    double[][] partialBofZ;
    double[] preX;
    double[] partialMM;

    int globalColCount;
    int targetDimension;
    int blockSize;

    int threadRowCount;

    Stopwatch timer;
    long time = 0L;

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

    public MMWorker(int threadIdx, int globalColCount, int targetDimension, int blockSize, int threadRowCount){
        this.threadIdx = threadIdx;
        this.globalColCount = globalColCount;
        this.targetDimension = targetDimension;
        this.blockSize = blockSize;
        this.threadRowCount = threadRowCount;
        /* Allocate arrays */
        allocateArrays();

        /* To keep things simple let's take data initialization out of the mmLoop*/
        initializeData();

        timer = Stopwatch.createUnstarted();
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
        partialMM = new double[threadRowCount * targetDimension];
        partialBofZ = new double[threadRowCount][globalColCount];
    }

    public void run() {
        timer.start();
        MatrixUtils
                .matrixMultiply(partialBofZ, preX, threadRowCount,
                        targetDimension, globalColCount, blockSize, partialMM);
        timer.stop();
        time += timer.elapsed(TimeUnit.MILLISECONDS);
        timer.reset();
    }

    public long getTime(){
        return time;
    }
}
