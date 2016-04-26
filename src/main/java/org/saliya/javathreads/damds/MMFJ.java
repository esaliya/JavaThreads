package org.saliya.javathreads.damds;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public abstract class MMFJ {
    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch timer;
    static Stopwatch commTimer;
    static Stopwatch compTimer;
    static Stopwatch[] compInternalTimer;

    static long sumTime = 0L, sumCompTime = 0L, sumCommTime =0L;
    static long time = 0L, compTime = 0L, maxCompTime = 0L, commTime=0L;

    public static void mmLoop(int iterations, MMWorker[] workers){
        /* Start main mmLoop*/
        for (int itr = 0; itr < iterations; ++itr) {
            timer.start();
            if (ParallelOps.threadCount > 1){
                compTimer.start();
                launchHabaneroApp(
                        () -> forallChunked(
                                0, ParallelOps.threadCount - 1,
                                (threadIdx) -> {
                                    compInternalTimer[threadIdx].start();
                                    workers[threadIdx].run();
                                    compInternalTimer[threadIdx].stop();
                                }));
                compTimer.stop();
            } else {
                compTimer.start();
                compInternalTimer[0].start();
                workers[0].run();
                compInternalTimer[0].stop();
                compTimer.stop();
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

            MMUtils.printMessage("Iteration " + itr + " time " + time +" ms compute " + compTime + " max-internal-comp " + maxCompTime + " ms comm " + commTime + " ms");
        }
        MMUtils.printMessage("Total time " + sumTime +" ms compute " +
                sumCompTime + " ms comm " + sumCommTime + " ms");
    }
}
