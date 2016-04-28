package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import org.saliya.javathreads.MatrixUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MMWorker {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
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
    Date start, end;
    Date threadStart, threadEnd;
    private Date threadStartAndEnd;

    public MMWorker(int threadIdx, double[][] partialBofZ, double[] preX,
                    double[] partialMM,
                    int globalColCount, int targetDimension, int blockSize, int threadRowCount) {
        this.threadIdx = threadIdx;
        this.partialBofZ = partialBofZ;
        this.preX = preX;
        this.partialMM = partialMM;
        this.globalColCount = globalColCount;
        this.targetDimension = targetDimension;
        this.blockSize = blockSize;
        this.threadRowCount = threadRowCount;
        timer = Stopwatch.createUnstarted();

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
        start = new Date();
        timer.start();
        MatrixUtils
                .matrixMultiply(partialBofZ, preX, threadRowCount,
                        targetDimension, globalColCount, blockSize, partialMM);
        timer.stop();
        time += timer.elapsed(TimeUnit.MILLISECONDS);
        timer.reset();
        end = new Date();
    }

    public long getTime(){
        return time;
    }

    public void setThreadStartAndEnd(Date threadStart, Date threadEnd) {
        this.threadStart = threadStart;
        this.threadEnd = threadEnd;
    }

    public String getTimeString(){
        return "threadStart " + sdf.format(threadStart) + " threadEnd " + sdf.format(threadEnd) + " compStart " + sdf.format(start) + " compEnd "  +sdf.format(end);
    }
}
