package org.saliya.javathreads.damds.local;

import com.google.common.base.Stopwatch;
import mpi.MPIException;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMFJLocal extends MMFJ{

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

        /* Initialize timers */
        timer = Stopwatch.createUnstarted();
        compTimer = Stopwatch.createUnstarted();
        compInternalTimer = new Stopwatch[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> compInternalTimer[i] = Stopwatch.createUnstarted());
        commTimer = Stopwatch.createUnstarted();

        MMWorker[] workers = new MMWorker[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> workers[i] =
                new MMWorker(i, globalColCount, targetDimension, blockSize));

        ParallelOps.worldProcsComm.barrier();
        mmLoop(iterations, workers);
        ParallelOps.tearDownParallelism();
    }
}
