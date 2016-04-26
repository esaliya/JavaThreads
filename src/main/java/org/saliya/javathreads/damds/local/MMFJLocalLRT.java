package org.saliya.javathreads.damds.local;

import mpi.MPIException;
import org.saliya.javathreads.damds.*;

import java.io.IOException;
import java.util.stream.IntStream;

public class MMFJLocalLRT extends MMLRT{

    public static void main(String[] args)
            throws MPIException, InterruptedException, IOException {
        setup(args);
        MMUtils.printMessage("Running in Local Data LRT Mode");

        MMWorker[] workers = new MMWorker[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> workers[i] =
                new MMWorker(i, globalColCount, targetDimension, blockSize));

        ParallelOps.worldProcsComm.barrier();
        mmLoop(workers);
        ParallelOps.tearDownParallelism();
    }
}
