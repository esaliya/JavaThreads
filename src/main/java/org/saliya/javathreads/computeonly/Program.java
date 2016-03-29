package org.saliya.javathreads.computeonly;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class Program {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);

        int rank = MPI.COMM_WORLD.getRank();
        if (threadCount > 1){
            launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                calcManager(iterations, threadIdx, rank);
            }));
        } else {
           calcManager(iterations, 0, rank);
        }

        MPI.Finalize();
    }

    private static void calcManager(int iterations, Integer threadIdx, int rank) {
        Stopwatch calcTimer = Stopwatch.createUnstarted();
        double avgCalcTime = 0.0;
        double maxCalcTime = Double.MIN_VALUE;
        double minCalcTime = Double.MAX_VALUE;
        double t;
        for (int i = 0; i < iterations; ++i){
            calcTimer.start();
            calc();
            calcTimer.stop();
            t = calcTimer.elapsed(TimeUnit.MILLISECONDS);
            calcTimer.reset();
            avgCalcTime += t;
            if (maxCalcTime < t) maxCalcTime = t;
            if (minCalcTime > t) minCalcTime = t;
        }
        System.out.println("Rank: " + rank  + "Thread: " + threadIdx + " avg: " + (avgCalcTime / iterations) + " min: " + minCalcTime + " max: " + maxCalcTime);
    }


    public static double calc() {
        double ee = 5.2345;
        for(int i = 0; i < 30; i++) {
            for(int j = 0; j < 10000000; j++) {
                double e = 0.9999995;
                ee = ee * (e / 1.0000023);
            }
        }
        return ee;
    }
}
