package org.saliya.javathreads.damds.local;

import mpi.MPIException;
import org.saliya.javathreads.damds.MMFJ;
import org.saliya.javathreads.damds.MMWorker;
import org.saliya.javathreads.damds.ParallelOps;

import java.io.IOException;
import java.util.stream.IntStream;

public class MMFJLocal extends MMFJ{

    public static void main(String[] args)
            throws MPIException, InterruptedException, IOException {
        setup(args);

        MMWorker[] workers = new MMWorker[ParallelOps.threadCount];
        IntStream.range(0, ParallelOps.threadCount).forEach(i -> workers[i] =
                new MMWorker(i, globalColCount, targetDimension, blockSize));

        ParallelOps.worldProcsComm.barrier();
        mmLoop(workers);
        ParallelOps.tearDownParallelism();
    }
}
