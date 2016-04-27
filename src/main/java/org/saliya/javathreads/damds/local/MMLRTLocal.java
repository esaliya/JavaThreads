package org.saliya.javathreads.damds.local;

import mpi.MPIException;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MMLRTLocal extends MMLRT{

    public static void main(String[] args)
            throws MPIException, InterruptedException, IOException {
        setup(args);
        MMUtils.printMessage("Running in Local Data LRT Mode");

        ParallelOps.worldProcsComm.barrier();
        timer.start();
        mmLoopLocalData(ParallelOps.threadRowCounts);
        ParallelOps.worldProcsComm.barrier();
        timer.stop();
        MMUtils.printMessage("Total time " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");
        ParallelOps.tearDownParallelism();
    }
}
