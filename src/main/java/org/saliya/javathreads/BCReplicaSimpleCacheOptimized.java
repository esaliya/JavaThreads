package org.saliya.javathreads;

import com.google.common.base.Stopwatch;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class BCReplicaSimpleCacheOptimized {
    static double [][] threadPartialBofZ;
    static double [] preX;
    static double [][] threadPartialOutMM;
    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch[] timers;

    static double[] busySqrtResults;

    public static void main(String[] args) {
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);

        final int globalColCount = args.length > 2 ? Integer.parseInt(args[2]): 50000;
        final int rowCountPerUnit = args.length > 3 ? Integer.parseInt(args[3]): 100;

        preX = new double[globalColCount*targetDimension];
        threadPartialOutMM = new double[threadCount][rowCountPerUnit*targetDimension];
        threadPartialBofZ = new double[threadCount][rowCountPerUnit*globalColCount];
        timers = new Stopwatch[threadCount];

        busySqrtResults = new double[threadCount];

        Stopwatch miscTimer = Stopwatch.createUnstarted();
        Stopwatch hjAppTimer = Stopwatch.createUnstarted();
        Stopwatch dataInitTimer = Stopwatch.createUnstarted();
        long miscTime = 0L;
        long hjAppTime = 0L;
        long dataInitTime = 0L;
        double computeTotalTime = 0.0;
        Stopwatch loopTimer = Stopwatch.createStarted();
        for (int itr = 0; itr < iterations; ++itr) {
            dataInitTimer.start();
            IntStream.range(0, globalColCount).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                        preX[r*targetDimension+c] = Math.random();
                    }));
            IntStream.range(0,threadCount).forEach(t -> IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, globalColCount).forEach(c -> {
                            threadPartialBofZ[t][r*globalColCount+c] = Math.random();
                        })));
            IntStream.range(0,threadCount).forEach(t -> IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                            threadPartialOutMM[t][r*targetDimension+c] = 0.0;
                        })));
            IntStream.range(0,threadCount).forEach(t -> timers[t] = Stopwatch.createUnstarted());
            dataInitTimer.stop();

            if (threadCount > 1) {
                hjAppTimer.start();
                launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                        timers[threadIdx].start();
                        MatrixUtils
                            .matrixMultiply(threadPartialBofZ[threadIdx], preX,
                                rowCountPerUnit, targetDimension, globalColCount, blockSize,
                                threadPartialOutMM[threadIdx]);

                        // Note - now what if we replace matrix multiply with busysqrt
                        /*busySqrt(busySqrtResults, threadIdx);*/

                        // Note - now see with naive MM
                        /*naiveMM(threadPartialBofZ[threadIdx], preX, threadPartialOutMM[threadIdx], rowCountPerUnit, globalColCount, targetDimension);*/
                        timers[threadIdx].stop();
                    }));
                hjAppTimer.stop();
            } else{
                timers[0].start();
                MatrixUtils
                    .matrixMultiply(threadPartialBofZ[0], preX,
                        rowCountPerUnit, targetDimension, globalColCount, blockSize,
                        threadPartialOutMM[0]);
                timers[0].stop();
            }


            miscTimer.start();
            DoubleSummaryStatistics ds = Arrays.stream(timers).collect(Collectors.summarizingDouble(timer->timer.elapsed(TimeUnit.MILLISECONDS)));
            computeTotalTime += ds.getMax();
            System.out.println(
                "Iteration: " + itr + " dataInitTime: " + dataInitTimer
                    .elapsed(TimeUnit.MILLISECONDS) + " ms" + ((threadCount>1)?(" hjAppTime: "
                + hjAppTimer.elapsed(TimeUnit.MILLISECONDS) + " ms "):"") + " ThreadMin: " + ds.getMin() + " ms ThreadMax: " + ds.getMax() + " ms ThreadAvg: " + ds.getAverage() + " ms");
            miscTimer.stop();

            dataInitTime += dataInitTimer
                .elapsed(TimeUnit.MILLISECONDS);
            if (threadCount > 1) {
                hjAppTime += hjAppTimer.elapsed(TimeUnit.MILLISECONDS);
            }
            miscTime += miscTimer.elapsed(TimeUnit.MILLISECONDS);


            dataInitTimer.reset();
            hjAppTimer.reset();
            miscTimer.reset();
        }
        loopTimer.stop();
        final long sumOfCompsTotal = dataInitTime + hjAppTime + miscTime;
        final long loopTotal = loopTimer.elapsed(
            TimeUnit.MILLISECONDS);
        System.out.println(
            "Compute Avg: " + (computeTotalTime / iterations) + " ms Loop Total: " + loopTotal + " ms dataInitTotal: " + dataInitTime
            + " ms" + (threadCount > 1 ? (" hjAppTimeTotal: " + hjAppTime + " ms"): "") + " miscTimeTotal: "
            + miscTime + " ms SumOfCompsTotal: " + sumOfCompsTotal
            + " ms AnyOtherDiff: " + (loopTotal - sumOfCompsTotal) + "ms");
    }

    private static void busySqrt(double[]results, int threadIdx) {
        double x = Math.random()*1e10;
        for (int i = 0; i < 15000000; ++i){
            x = Math.sqrt(x*Math.random()*1.e10);
        }
        results[threadIdx] = x;
    }

    private static void naiveMM(double[][]A, double[][]B, double[][]C, int rows, int common, int cols) {
        for (int i = 0; i < rows; ++i){
            for (int j = 0; j < cols; ++j){
                for (int k = 0; k < common; ++k){
//                    C[i][j] += A[i][k]*B[k][j];
//                    C[i][j] += Math.sqrt(Math.random());
                    C[i][j] += A[i][k];
                }
            }
        }
    }
}
