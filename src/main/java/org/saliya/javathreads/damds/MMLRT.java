package org.saliya.javathreads.damds;

import mpi.MPIException;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class MMLRT extends MM {
    public static void mmLoop(MMWorker[] workers) throws MPIException {
        /* Start main mmLoop*/
        if (ParallelOps.threadCount > 1){
            timer.start();
            compTimer.start();
            launchHabaneroApp(
                    () -> forallChunked(
                            0, ParallelOps.threadCount - 1,
                            (threadIdx) -> {
                                for (int itr = 0; itr < iterations; ++itr) {
                                    workers[threadIdx].run();
                                }
                            }));
            compTimer.stop();
            timer.stop();
        } else {
            timer.start();
            compTimer.start();
            for (int itr = 0; itr < iterations; ++itr) {
                workers[0].run();
            }
            compTimer.stop();
            timer.stop();
        }

        time = timer.elapsed(TimeUnit.MILLISECONDS);
        sumTime += time;

        compTime = compTimer.elapsed(TimeUnit.MILLISECONDS);
        sumCompTime +=compTime;

        timer.reset();
        compTimer.reset();

        MMUtils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms");
    }
}
