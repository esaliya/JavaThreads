package org.saliya.javathreads.damds.global;

import com.google.common.base.Stopwatch;
import mpi.MPIException;
import net.openhft.lang.io.Bytes;
import org.saliya.javathreads.*;
import org.saliya.javathreads.Utils;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMFJGlobal {
    static double [][][] threadPartialBofZ;
    static double [] preX;
    static double [][] threadPartialMM;
    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch timer;
    static Stopwatch commTimer;
    static Stopwatch compTimer;
    static Stopwatch[] compInternalTimer;

    static long sumTime = 0L, sumCompTime = 0L, sumCommTime =0L;
    static long time = 0L, compTime = 0L, maxCompTime = 0L, commTime=0L;


    public static void main(String[] args)
        throws MPIException, InterruptedException, IOException {
        /* Set configuration options */
        final int iterations = Integer.parseInt(args[0]);
        final int globalColCount = Integer.parseInt(args[1]);
        ParallelOps.nodeCount = Integer.parseInt(args[2]);
        blockSize = (args.length > 3) ? Integer.parseInt(args[3]) : 64;
        ParallelOps.threadCount = (args.length > 4) ? Integer.parseInt(args[4]) : 1;
        ParallelOps.mmapScratchDir = (args.length > 5) ? args[5] : "/dev/shm";

        ParallelOps.mmapsPerNode = 1;

        /* Set up parallelism */
        ParallelOps.setupParallelism(args);
        ParallelOps.setParallelDecomposition(globalColCount, targetDimension);


        /* Allocate arrays */
        allocateArrays(globalColCount);

        /* To keep things simple let's take data initialization out of the loop*/
        initializeData(globalColCount);

        /* Initialize timers */
        timer = Stopwatch.createUnstarted();
        compTimer = Stopwatch.createUnstarted();
        compInternalTimer = new Stopwatch[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> compInternalTimer[i] = Stopwatch.createUnstarted());
        commTimer = Stopwatch.createUnstarted();

        Worker[] workers = new Worker[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> workers[i] =
                new Worker(i, threadPartialBofZ[i], preX, threadPartialMM[i],
                        globalColCount, targetDimension, blockSize));

        /* Start main loop*/
        for (int itr = 0; itr < iterations; ++itr) {
            timer.start();
            if (ParallelOps.threadCount > 1){
                compTimer.start();
                launchHabaneroApp(
                        () -> forallChunked(
                                0, ParallelOps.threadCount - 1,
                                (threadIdx) -> {
                                    compInternalTimer[threadIdx].start();
                                    MatrixUtils
                                            .matrixMultiply(threadPartialBofZ[threadIdx], preX, ParallelOps.threadRowCounts[threadIdx],
                                                    targetDimension, globalColCount, blockSize, threadPartialMM[threadIdx]);
                                    compInternalTimer[threadIdx].stop();
                                }));
                compTimer.stop();
            } else {
                ParallelOps.worldProcsComm.barrier();


                compTimer.start();
                compInternalTimer[0].start();
                MatrixUtils
                        .matrixMultiply(threadPartialBofZ[0], preX, ParallelOps.threadRowCounts[0],

                                targetDimension, globalColCount, blockSize, threadPartialMM[0]);
                compInternalTimer[0].stop();
                compTimer.stop();
            }

            if (ParallelOps.worldProcsCount > 1){

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
            }
            timer.stop();

            time = timer.elapsed(TimeUnit.MILLISECONDS);
            sumTime += time;

            compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
            sumCompTime +=compTime;
            maxCompTime = IntStream.range(0, ParallelOps.threadCount).mapToLong(i -> compInternalTimer[i].elapsed(TimeUnit.MILLISECONDS)).max().getAsLong();

            commTime = commTimer.elapsed(TimeUnit.MILLISECONDS);
            sumCommTime +=commTime;


            timer.reset();
            compTimer.reset();
            IntStream.range(0, ParallelOps.threadCount).forEach(i -> compInternalTimer[i].reset());
            commTimer.reset();

            org.saliya.javathreads.damds.Utils.printMessage("Iteration " + itr + " time " + time +" ms compute " + compTime + " max-internal-comp " + maxCompTime + " ms comm " + commTime + " ms");
        }
        org.saliya.javathreads.damds.Utils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms comm " + sumCommTime + " ms");



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
        org.saliya.javathreads.damds.Utils.generatePreX(globalColCount,
                targetDimension, preX);

        /* Set numbers for BofZ*/
        IntStream.range(0, ParallelOps.threadCount).forEach(
                t -> org.saliya.javathreads.damds.Utils.generateBofZ(t,
                        globalColCount, threadPartialBofZ[t]));
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
