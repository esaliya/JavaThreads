package org.saliya.javathreads;

import com.google.common.base.Stopwatch;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class BCReplicaSimpleIndependentArrays {
    static double [][]threadPartialBofZ0;
    static double [][]threadPartialBofZ1;
    static double [][]threadPartialBofZ2;
    static double [][]threadPartialBofZ3;
    static double [][]threadPartialBofZ4;
    static double [][]threadPartialBofZ5;
    static double [][]threadPartialBofZ6;
    static double [][]threadPartialBofZ7;
    static double [][]threadPartialBofZ8;
    static double [][]threadPartialBofZ9;
    static double [][]threadPartialBofZ10;
    static double [][]threadPartialBofZ11;
    static double [][]threadPartialBofZ12;
    static double [][]threadPartialBofZ13;
    static double [][]threadPartialBofZ14;
    static double [][]threadPartialBofZ15;
    static double [][]threadPartialBofZ16;
    static double [][]threadPartialBofZ17;
    static double [][]threadPartialBofZ18;
    static double [][]threadPartialBofZ19;
    static double [][]threadPartialBofZ20;
    static double [][]threadPartialBofZ21;
    static double [][]threadPartialBofZ22;
    static double [][]threadPartialBofZ23;

    static double [][] preX;

    static double [][] threadPartialOutMM0;
    static double [][] threadPartialOutMM1;
    static double [][] threadPartialOutMM2;
    static double [][] threadPartialOutMM3;
    static double [][] threadPartialOutMM4;
    static double [][] threadPartialOutMM5;
    static double [][] threadPartialOutMM6;
    static double [][] threadPartialOutMM7;
    static double [][] threadPartialOutMM8;
    static double [][] threadPartialOutMM9;
    static double [][] threadPartialOutMM10;
    static double [][] threadPartialOutMM11;
    static double [][] threadPartialOutMM12;
    static double [][] threadPartialOutMM13;
    static double [][] threadPartialOutMM14;
    static double [][] threadPartialOutMM15;
    static double [][] threadPartialOutMM16;
    static double [][] threadPartialOutMM17;
    static double [][] threadPartialOutMM18;
    static double [][] threadPartialOutMM19;
    static double [][] threadPartialOutMM20;
    static double [][] threadPartialOutMM21;
    static double [][] threadPartialOutMM22;
    static double [][] threadPartialOutMM23;


    static int targetDimension = 3;
    static int blockSize = 64;
    static Stopwatch[] timers;

    static double[] busySqrtResults;

    public static void main(String[] args) {
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);

        final int globalColCount = args.length > 2 ? Integer.parseInt(args[2]): 50000;
        final int rowCountPerUnit = args.length > 3 ? Integer.parseInt(args[3]): 100;

        preX = new double[globalColCount][targetDimension];

        threadPartialOutMM0 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM1 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM2 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM3 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM4 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM5 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM6 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM7 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM8 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM9 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM10 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM11 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM12 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM13 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM14 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM15 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM16 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM17 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM18 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM19 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM20 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM21 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM22 = new double[rowCountPerUnit][targetDimension];
        threadPartialOutMM23 = new double[rowCountPerUnit][targetDimension];

        threadPartialBofZ0 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ1 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ2 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ3 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ4 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ5 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ6 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ7 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ8 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ9 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ10 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ11 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ12 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ13 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ14 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ15 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ16 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ17 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ18 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ19 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ20 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ21 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ22 = new double[rowCountPerUnit][globalColCount];
        threadPartialBofZ23 = new double[rowCountPerUnit][globalColCount];

        timers = new Stopwatch[threadCount];

        busySqrtResults = new double[threadCount];

        Stopwatch miscTimer = Stopwatch.createUnstarted();
        Stopwatch hjAppTimer = Stopwatch.createUnstarted();
        Stopwatch dataInitTimer = Stopwatch.createUnstarted();
        long miscTime = 0L;
        long hjAppTime = 0L;
        long dataInitTime = 0L;
        Stopwatch loopTimer = Stopwatch.createStarted();
        for (int itr = 0; itr < iterations; ++itr) {
            dataInitTimer.start();
            IntStream.range(0, globalColCount).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                        preX[r][c] = Math.random();
                    }));
            IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, globalColCount).forEach(c -> {
                            threadPartialBofZ0[r][c] = Math.random();
                            threadPartialBofZ1[r][c] = Math.random();
                            threadPartialBofZ2[r][c] = Math.random();
                            threadPartialBofZ3[r][c] = Math.random();
                            threadPartialBofZ4[r][c] = Math.random();
                            threadPartialBofZ5[r][c] = Math.random();
                            threadPartialBofZ6[r][c] = Math.random();
                            threadPartialBofZ7[r][c] = Math.random();
                            threadPartialBofZ8[r][c] = Math.random();
                            threadPartialBofZ9[r][c] = Math.random();
                            threadPartialBofZ10[r][c] = Math.random();
                            threadPartialBofZ11[r][c] = Math.random();
                            threadPartialBofZ12[r][c] = Math.random();
                            threadPartialBofZ13[r][c] = Math.random();
                            threadPartialBofZ14[r][c] = Math.random();
                            threadPartialBofZ15[r][c] = Math.random();
                            threadPartialBofZ16[r][c] = Math.random();
                            threadPartialBofZ17[r][c] = Math.random();
                            threadPartialBofZ18[r][c] = Math.random();
                            threadPartialBofZ19[r][c] = Math.random();
                            threadPartialBofZ20[r][c] = Math.random();
                            threadPartialBofZ21[r][c] = Math.random();
                            threadPartialBofZ22[r][c] = Math.random();
                            threadPartialBofZ23[r][c] = Math.random();
                        }));

            IntStream.range(0, rowCountPerUnit).forEach(r -> IntStream.range(0, targetDimension).forEach(c -> {
                        threadPartialOutMM0[r][c] = 0.0d;
                        threadPartialOutMM1[r][c] = 0.0d;
                        threadPartialOutMM2[r][c] = 0.0d;
                        threadPartialOutMM3[r][c] = 0.0d;
                        threadPartialOutMM4[r][c] = 0.0d;
                        threadPartialOutMM5[r][c] = 0.0d;
                        threadPartialOutMM6[r][c] = 0.0d;
                        threadPartialOutMM7[r][c] = 0.0d;
                        threadPartialOutMM8[r][c] = 0.0d;
                        threadPartialOutMM9[r][c] = 0.0d;
                        threadPartialOutMM10[r][c] = 0.0d;
                        threadPartialOutMM11[r][c] = 0.0d;
                        threadPartialOutMM12[r][c] = 0.0d;
                        threadPartialOutMM13[r][c] = 0.0d;
                        threadPartialOutMM14[r][c] = 0.0d;
                        threadPartialOutMM15[r][c] = 0.0d;
                        threadPartialOutMM16[r][c] = 0.0d;
                        threadPartialOutMM17[r][c] = 0.0d;
                        threadPartialOutMM18[r][c] = 0.0d;
                        threadPartialOutMM19[r][c] = 0.0d;
                        threadPartialOutMM20[r][c] = 0.0d;
                        threadPartialOutMM21[r][c] = 0.0d;
                        threadPartialOutMM22[r][c] = 0.0d;
                        threadPartialOutMM23[r][c] = 0.0d;
                        }));

            IntStream.range(0,threadCount).forEach(t -> timers[t] = Stopwatch.createUnstarted());
            dataInitTimer.stop();

            hjAppTimer.start();
            launchHabaneroApp(
                () -> forallChunked(
                    0, threadCount - 1, (threadIdx) -> {
                        timers[threadIdx].start();
                        /*MatrixUtils.matrixMultiply(
                            threadPartialBofZ[threadIdx], preX, rowCountPerUnit,
                            targetDimension, globalColCount, blockSize,
                            threadPartialOutMM[threadIdx]);*/

                        // Note - now what if we replace matrix multiply with busysqrt
                        /*busySqrt(busySqrtResults, threadIdx);*/

                        // Note - now see with naive MM
                        if (threadIdx == 0) {
                            naiveMM(
                                threadPartialBofZ0, preX, threadPartialOutMM0,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 1) {
                            naiveMM(
                                threadPartialBofZ1, preX, threadPartialOutMM1,
                                rowCountPerUnit, globalColCount, targetDimension);
                        }  else if (threadIdx == 2) {
                            naiveMM(
                                threadPartialBofZ2, preX, threadPartialOutMM2,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 3) {
                            naiveMM(
                                threadPartialBofZ3, preX, threadPartialOutMM3,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 4) {
                            naiveMM(
                                threadPartialBofZ4, preX, threadPartialOutMM4,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 5) {
                            naiveMM(
                                threadPartialBofZ5, preX, threadPartialOutMM5,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 6) {
                            naiveMM(
                                threadPartialBofZ6, preX, threadPartialOutMM6,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 7) {
                            naiveMM(
                                threadPartialBofZ7, preX, threadPartialOutMM7,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 8) {
                            naiveMM(
                                threadPartialBofZ8, preX, threadPartialOutMM8,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 9) {
                            naiveMM(
                                threadPartialBofZ9, preX, threadPartialOutMM9,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 10) {
                            naiveMM(
                                threadPartialBofZ10, preX, threadPartialOutMM10,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 11) {
                            naiveMM(
                                threadPartialBofZ11, preX, threadPartialOutMM11,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 12) {
                            naiveMM(
                                threadPartialBofZ12, preX, threadPartialOutMM12,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 13) {
                            naiveMM(
                                threadPartialBofZ13, preX, threadPartialOutMM13,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 14) {
                            naiveMM(
                                threadPartialBofZ14, preX, threadPartialOutMM14,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 15) {
                            naiveMM(
                                threadPartialBofZ15, preX, threadPartialOutMM15,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 16) {
                            naiveMM(
                                threadPartialBofZ16, preX, threadPartialOutMM16,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 17) {
                            naiveMM(
                                threadPartialBofZ17, preX, threadPartialOutMM17,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 18) {
                            naiveMM(
                                threadPartialBofZ18, preX, threadPartialOutMM18,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 19) {
                            naiveMM(
                                threadPartialBofZ19, preX, threadPartialOutMM19,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 20) {
                            naiveMM(
                                threadPartialBofZ20, preX, threadPartialOutMM20,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 21) {
                            naiveMM(
                                threadPartialBofZ21, preX, threadPartialOutMM21,
                                rowCountPerUnit, globalColCount, targetDimension);
                        } else if (threadIdx == 22) {
                            naiveMM(
                                threadPartialBofZ22, preX, threadPartialOutMM22,
                                rowCountPerUnit, globalColCount, targetDimension);
                        }else if (threadIdx == 23) {
                            naiveMM(
                                threadPartialBofZ23, preX, threadPartialOutMM23,
                                rowCountPerUnit, globalColCount, targetDimension);
                        }
                        timers[threadIdx].stop();
                    }));
            hjAppTimer.stop();

            miscTimer.start();
            DoubleSummaryStatistics ds = Arrays.stream(timers).collect(Collectors.summarizingDouble(timer->timer.elapsed(TimeUnit.MILLISECONDS)));
            System.out.println(
                "Iteration: " + itr + " dataInitTime: " + dataInitTimer
                    .elapsed(TimeUnit.MILLISECONDS) + " ms hjAppTime: "
                + hjAppTimer.elapsed(TimeUnit.MILLISECONDS) + " ms " + " ThreadMin: " + ds.getMin() + " ms ThreadMax: " + ds.getMax() + " ms ThreadAvg: " + ds.getAverage() + " ms");
            miscTimer.stop();

            dataInitTime += dataInitTimer
                .elapsed(TimeUnit.MILLISECONDS);
            hjAppTime += hjAppTimer.elapsed(TimeUnit.MILLISECONDS);
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
            "Loop Total: " + loopTotal + " ms dataInitTotal: " + dataInitTime
            + " ms hjAppTimeTotal: " + hjAppTime + " ms miscTimeTotal: "
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
