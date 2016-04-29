package org.saliya.javathreads.damds.global;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import net.openhft.affinity.Affinity;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.BitSet;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMLRTGlobal{
    private static int targetDimension = 3;
    private static int blockSize = 64;
    private static int iterations;
    private static int globalColCount;
    private static int threadCount;
    private static Stopwatch timer;

    private static LongBuffer times;
    private static MMWorker[] mmWorkers;

    static double [][][] threadPartialBofZ;
    static double [][] preX;
    static double [][] threadPartialMM;

    public static void main(String[] args)
        throws MPIException, InterruptedException, IOException {
        setup(args);
        times = MPI.newLongBuffer(ParallelOps.worldProcsCount * threadCount);
        mmWorkers = new MMWorker[threadCount];

        MMUtils.printMessage("Running in Global Data LRT Mode");

        /* Allocate global arrays */
        allocateArrays(globalColCount);
        /* To keep things simple let's take data initialization out of the mmLoopLocalData*/
        initializeData(globalColCount);

        ParallelOps.worldProcsComm.barrier();
        timer.start();
        mmLoopGlobalData(threadPartialBofZ, preX, threadPartialMM, ParallelOps.threadRowCounts);
        ParallelOps.worldProcsComm.barrier();
        timer.stop();

        IntStream.range(0, threadCount).forEach(i -> times.put(i, mmWorkers[i].getTime()));
        ParallelOps.gather(times, threadCount, 0);
//        IntStream.range(0, threadCount*ParallelOps.worldProcsCount).forEach(i -> MMUtils.printMessage("Rank " + (i/threadCount) + " Thread " + (i%threadCount) + " comp time " + times.get(i) + " ms" ));

        IntStream.range(0, threadCount).forEach(i -> {
            System.out.println("Rank " + ParallelOps.worldProcRank + " Thread " + i  + " " + mmWorkers[i].getTimeString());
        });
        MMUtils.printMessage("Total time " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");

        ParallelOps.tearDownParallelism();
    }

    public static void mmLoopGlobalData(double[][][] threadPartialBofZ, double[][] preX,
                                        double[][] threadPartialMM, int[] threadRowCounts) throws MPIException {

        /* Start main mmLoopLocalData*/
        if (ParallelOps.threadCount > 1){
            launchHabaneroApp(
                    () -> forallChunked(
                            0, ParallelOps.threadCount - 1,
                            (threadIdx) -> {
                                Date threadStart = new Date();
                                BitSet bitSet = new BitSet(48);
                                // TODO - let's hard code for juliet 12x2 for now
                                bitSet.set(((ParallelOps.worldProcRank%2) * 12) +
                                        threadIdx);
                                bitSet.set(((ParallelOps.worldProcRank%2) * 24) +
                                        threadIdx + 24);
                                Affinity.setAffinity(bitSet);

                                MMWorker mmWorker = new MMWorker
                                        (threadIdx, threadPartialBofZ[threadIdx], preX[threadIdx],
                                                threadPartialMM[threadIdx],
                                                globalColCount, targetDimension,
                                                blockSize, threadRowCounts[threadIdx]);
                                mmWorkers[threadIdx] = mmWorker;
                                for (int itr = 0; itr < iterations; ++itr) {
                                    mmWorker.run();
                                }
                                Date threadEnd = new Date();
                                mmWorker.setThreadStartAndEnd(threadStart, threadEnd);
                            }));
        } else {
            Date threadStart = new Date();
            MMWorker mmWorker = new MMWorker
                    (0, threadPartialBofZ[0], preX[0],
                            threadPartialMM[0],
                            globalColCount, targetDimension,
                            blockSize, threadRowCounts[0]);
            mmWorkers[0] = mmWorker;
            for (int itr = 0; itr < iterations; ++itr) {
                mmWorker.run();
            }
            Date threadEnd = new Date();
            mmWorker.setThreadStartAndEnd(threadStart, threadEnd);
        }
    }

    private static void allocateArrays(int globalColCount) {
        preX = new double[ParallelOps.threadCount][globalColCount*targetDimension];
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
        IntStream.range(0, ParallelOps.threadCount).forEach(i ->
        MMUtils.generatePreX(globalColCount,
                targetDimension, preX[i]));

        /* Set numbers for BofZ*/
        IntStream.range(0, ParallelOps.threadCount).forEach(
                t -> MMUtils.generateBofZ(t,
                        globalColCount, threadPartialBofZ[t]));
    }

    public static void setup(String[] args) throws MPIException, IOException {
         /* Set configuration options */
        parseArgs(args);

        /* Set up parallelism */
        ParallelOps.setupParallelism(args);
        ParallelOps.setParallelDecomposition(globalColCount, targetDimension);

        /* Initialize timers */
        initializeTimers();
    }

    private static void initializeTimers(){
        timer = Stopwatch.createUnstarted();
    }

    private static void parseArgs(String[] args){
        /* Set configuration options */
        iterations = Integer.parseInt(args[0]);
        globalColCount = Integer.parseInt(args[1]);
        ParallelOps.nodeCount = Integer.parseInt(args[2]);
        blockSize = (args.length > 3) ? Integer.parseInt(args[3]) : 64;
        threadCount = (args.length > 4) ? Integer.parseInt(args[4]) : 1;
        ParallelOps.threadCount = threadCount;
        ParallelOps.mmapScratchDir = (args.length > 5) ? args[5] : "/dev/shm";

        ParallelOps.mmapsPerNode = 1;
    }
}
