package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Interner;
import mpi.MPIException;
import net.openhft.lang.io.Bytes;
import org.saliya.javathreads.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MMMpi {
    static double [][][] threadPartialBofZ;
    static double [] preX;
    static double [][] threadPartialMM;
    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch timer;
    static Stopwatch commTimer;
    static Stopwatch compTimer;

    static long accumTime = 0L, accumCompTime = 0L, accumCommTime=0L;
    static long time = 0L, compTime = 0L, commTime=0L;


    public static void main(String[] args)
        throws MPIException, InterruptedException, IOException {
        /* Set configuration options */
        final int iterations = Integer.parseInt(args[0]);
        final int globalColCount = Integer.parseInt(args[1]);
        ParallelOps.nodeCount = Integer.parseInt(args[2]);
        ParallelOps.mmapScratchDir = (args.length > 3) ? args[3] : "/dev/shm";
        ParallelOps.threadCount = 1;
        ParallelOps.mmapsPerNode = 1;

        /* Set up parallelism */
        ParallelOps.setupParallelism(args);
        ParallelOps.setParallelDecomposition(globalColCount, targetDimension);


        /* Allocate arrays */
        preX = new double[globalColCount*targetDimension];
        threadPartialMM = new double[ParallelOps.threadCount][];
        threadPartialBofZ = new double[ParallelOps.threadCount][][];
        int threadRowCount;
        for (int i = 0; i < ParallelOps.threadCount; ++i){
            threadRowCount = ParallelOps.threadRowCounts[i];
            threadPartialBofZ[i] = new double[threadRowCount][ParallelOps.globalColCount];
            threadPartialMM[i] = new double[threadRowCount * targetDimension];
        }

        timer = Stopwatch.createUnstarted();
        compTimer = Stopwatch.createUnstarted();
        commTimer = Stopwatch.createUnstarted();


        for (int itr = 0; itr < iterations; ++itr) {
            /* Generate initial mapping of points */
            generateInitMapping(globalColCount, targetDimension, preX);

            /* Set random numbers for BofZ*/
            IntStream.range(0, ParallelOps.threadCount).forEach(
                t -> IntStream.range(0, ParallelOps.threadRowCounts[t]).forEach(
                    r -> IntStream.range(0, globalColCount).forEach(c -> {
                        threadPartialBofZ[t][r][c] = Math.random();
                    })));

            if (ParallelOps.threadCount > 1){
                Utils.printMessage("Threads are not supported in this version!");
            } else {
                ParallelOps.worldProcsComm.barrier();

                timer.start();
                compTimer.start();
                MatrixUtils
                    .matrixMultiply(threadPartialBofZ[0], preX, ParallelOps.threadRowCounts[0],
                        targetDimension, globalColCount, blockSize, threadPartialMM[0]);
                compTimer.stop();

                mergePartials(threadPartialMM, ParallelOps.mmapXWriteBytes);

                // Important barrier here - as we need to make sure writes are done to the mmap file
                // it's sufficient to wait on ParallelOps.mmapProcComm, but it's cleaner for timings
                // if we wait on the whole world
                ParallelOps.worldProcsComm.barrier();
                commTimer.start();

                if (ParallelOps.isMmapLead) {
                    ParallelOps.partialXAllGather();
                }
                // Each process in a memory group waits here.
                // It's not necessary to wait for a process
                // in another memory map group, hence the use of mmapProcComm.
                // However it's cleaner for any timings to have everyone sync here,
                // so will use worldProcsComm instead.
                ParallelOps.worldProcsComm.barrier();
                commTimer.stop();

                extractPoints(ParallelOps.fullXBytes,
                    ParallelOps.globalColCount,
                    targetDimension, preX);

                timer.stop();

                time = timer.elapsed(TimeUnit.MILLISECONDS);
                accumTime += time;
                compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
                accumCompTime+=compTime;
                commTime = commTimer.elapsed(TimeUnit.MILLISECONDS);
                accumCommTime+=commTime;

                timer.reset();
                compTimer.reset();
                commTimer.reset();

                Utils.printMessage("Iteration " + itr + " time " + time +" ms compute " + compTime + " ms comm " + commTime + " ms");
            }
        }
        Utils.printMessage("Total time " + accumTime +" ms compute " + accumCompTime + " ms comm " + accumCommTime + " ms");



        ParallelOps.tearDownParallelism();
    }

    private static void generateInitMapping(
        int numPoints, int targetDim, double[] preX) throws MPIException {

        Bytes fullBytes = ParallelOps.fullXBytes;
        if (ParallelOps.worldProcRank == 0) {
            int pos = 0;
            // Use Random class for generating random initial mapping solution.
            Random rand = new Random(System.currentTimeMillis());
            for (int i = 0; i < numPoints; i++) {
                for (int j = 0; j < targetDim; j++) {
                    fullBytes.position(pos);
                    fullBytes.writeDouble(rand.nextBoolean()
                        ? rand.nextDouble()
                        : -rand.nextDouble());
                    pos += Double.BYTES;
                }
            }
        }

        if (ParallelOps.worldProcsCount > 1){
            // Broadcast initial mapping to others
            ParallelOps.broadcast(ParallelOps.fullXByteBuffer, numPoints * targetDim*Double.BYTES, 0);
        }
        extractPoints(fullBytes, numPoints, targetDim, preX);
    }

    private static void extractPoints(
        Bytes bytes, int numPoints, int dimension, double[] to) {
        int pos = 0;
        int offset;
        for (int i = 0; i < numPoints; ++i){
            offset = i*dimension;
            for (int j = 0; j < dimension; ++j) {
                bytes.position(pos);
                to[offset+j] = bytes.readDouble(pos);
                pos += Double.BYTES;
            }
        }
    }

    private static void mergePartials(double[][] partials, double[] result){
        int offset = 0;
        for (double [] partial : partials){
            System.arraycopy(partial, 0, result, offset, partial.length);
            offset+=partial.length;
        }
    }

    private static void mergePartials(
        double[][] partials, Bytes result){
        result.position(0);
        for (double [] partial : partials){
            for (double aPartial : partial) {
                result.writeDouble(aPartial);
            }
        }
    }
}
