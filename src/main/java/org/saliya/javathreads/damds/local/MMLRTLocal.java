package org.saliya.javathreads.damds.local;

import com.google.common.base.Stopwatch;
import mpi.MPIException;
import net.openhft.affinity.Affinity;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMLRTLocal{
    private static int targetDimension = 3;
    private static int blockSize = 64;
    private static int iterations;
    private static int globalColCount;
    private static Stopwatch timer;

    public static void main(String[] args)
            throws MPIException, InterruptedException, IOException {
        setup(args);
        MMUtils.printMessage("Running in Local Data LRT Mode");

        ParallelOps.worldProcsComm.barrier();
        timer.start();
        mmLoopLocalData(ParallelOps.threadRowCounts);
        timer.stop();
        MMUtils.printMessage("Total time " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");
        ParallelOps.tearDownParallelism();
    }

    private static void mmLoopLocalData(int[] threadRowCounts){
        /* Start main mmLoopLocalData*/
        if (ParallelOps.threadCount > 1){
            launchHabaneroApp(
                    () -> forallChunked(
                            0, ParallelOps.threadCount - 1,
                            (threadIdx) -> {
                                BitSet bitSet = new BitSet(48);
                                // TODO - let's hard code for juliet 12x2 for now
                                bitSet.set(((ParallelOps.worldProcRank%2) * 12) +
                                        threadIdx);
                                bitSet.set(((ParallelOps.worldProcRank%2) * 24) +
                                        threadIdx + 24);
                                Affinity.setAffinity(bitSet);

                                MMWorker mmWorker = new MMWorker(threadIdx, globalColCount, targetDimension, blockSize, threadRowCounts[threadIdx]);
                                for (int itr = 0; itr < iterations; ++itr) {
                                    mmWorker.run();
                                }
                            }));
        } else {
            MMWorker mmWorker = new MMWorker(0, globalColCount, targetDimension, blockSize, threadRowCounts[0]);
            for (int itr = 0; itr < iterations; ++itr) {
                mmWorker.run();
            }
        }
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
        ParallelOps.threadCount = (args.length > 4) ? Integer.parseInt(args[4]) : 1;
        ParallelOps.mmapScratchDir = (args.length > 5) ? args[5] : "/dev/shm";

        ParallelOps.mmapsPerNode = 1;
    }
}
