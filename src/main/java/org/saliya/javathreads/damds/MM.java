package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;
import mpi.MPIException;

import java.io.IOException;
import java.util.stream.IntStream;

public abstract class MM {
    protected static int targetDimension = 3;
    protected static int blockSize = 64;
    static Stopwatch timer;
    static Stopwatch compTimer;

    static long sumTime = 0L, sumCompTime = 0L;
    static long time = 0L, compTime = 0L;

    static int iterations;
    protected static int globalColCount;

    public static void setup(String[] args) throws MPIException, IOException {
         /* Set configuration options */
        parseArgs(args);

        /* Set up parallelism */
        ParallelOps.setupParallelism(args);
        ParallelOps.setParallelDecomposition(globalColCount, targetDimension);

        /* Initialize timers */
        initializeTimers();
    }

    private static void parseArgs(String[] args){
        /* Set configuration options */
        iterations = Integer.parseInt(args[0]);
        globalColCount = Integer.parseInt(args[1]);
        ParallelOps.nodeCount = Integer.parseInt(args[2]);
        blockSize = (args.length > 3) ? Integer.parseInt(args[3]) : 64;
        ParallelOps.threadCount = (args.length > 4) ? Integer.parseInt(args[4]) : 1;
        ParallelOps.mmapScratchDir = (args.length > 5) ? args[5] : "/dev/shm";

        ParallelOps.mmapsPerNode = 1;
    }

    private static void initializeTimers(){
        timer = Stopwatch.createUnstarted();
        compTimer = Stopwatch.createUnstarted();
    }
}
