package org.saliya.javathreads.array;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import net.openhft.affinity.Affinity;
import org.saliya.javathreads.MatrixUtils;

import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class ProgramSimpleThreadsOuterloops {
    static class Worker implements Runnable{
        private final double[] times;
        int iterations;
        int threadIdx;
        int rank;
        int rows;
        int cols;
        int dim;
        CountDownLatch startLatch;
        CountDownLatch endLatch;

        Worker(
            int iterations, int threadIdx, int rank, int rows, int cols, int
            dim, CountDownLatch startLatch, CountDownLatch endLatch,
            double[] times){
            this.iterations = iterations;
            this.threadIdx = threadIdx;
            this.rank = rank;
            this.rows = rows;
            this.cols = cols;
            this.dim = dim;

            this.startLatch = startLatch;
            this.endLatch = endLatch;
            this.times = times;

        }
        @Override
        public void run() {
            BitSet bitSet = new BitSet(48);
            bitSet.set(threadIdx+1);
//            bitSet.set(threadIdx+1+24);
            Affinity.setAffinity(bitSet);

            double [] A = new double[rows*cols];
            double [] B = new double[cols*dim];
            double [] C = new double[rows*dim];
            double [] Adiag = new double[rows];
            for (int i = 0; i < rows*cols; ++i){
                A[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
            }

            for (int i = 0; i < cols*dim; ++i){
                B[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
            }
            for (int i = 0; i < rows*dim; ++i){
                C[i] = 0.0;
            }

            for (int i = 0; i < rows; ++i){
                Adiag[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
            }

            startLatch.countDown();
            try {
                startLatch.await();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Stopwatch mmTimer = Stopwatch.createUnstarted();
            double avgMMTime = 0.0;
            double maxMMTime = Double.MIN_VALUE;
            double minMMTime = Double.MAX_VALUE;
            double t;
            mmTimer.start();
            for (int i = 0; i < iterations; ++i){
                MatrixUtils.matrixMultiplyWithThreadOffset(A, Adiag, B, rows, dim, cols, 64, 0, 0, C);
            }

            mmTimer.stop();
            t = mmTimer.elapsed(TimeUnit.MILLISECONDS);
            mmTimer.reset();
            avgMMTime += t;
            if (maxMMTime < t) maxMMTime = t;
            if (minMMTime > t) minMMTime = t;
            times[threadIdx]+= t;
            endLatch.countDown();
//            System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);

        }
    }
    public static void main(String[] args)
        throws MPIException, InterruptedException {
        MPI.Init(args);
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);
        int rows = Integer.parseInt(args[2]);
        int cols = Integer.parseInt(args[3]);
        int dim = Integer.parseInt(args[4]);
        boolean hj = Boolean.parseBoolean(args[5]);
        int outerloops = Integer.parseInt(args[6]);

        int rank = MPI.COMM_WORLD.getRank();

        double[] worldTimes = null;
        double[] times = new double[threadCount];
        for (int i = 0; i < threadCount; ++i){
            times[i] = 0.0;
        }
        Stopwatch mainTimer = Stopwatch.createUnstarted();


        if (threadCount > 1){
            if (!hj) {
                System.out.println("Java Threads");
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                mainTimer.start();
                for (int loops = 0; loops < outerloops; ++loops) {
                    final CountDownLatch startLatch = new CountDownLatch(threadCount);
                    final CountDownLatch endLatch = new CountDownLatch(threadCount);
                    for (int i = 0; i < threadCount; ++i) {
                        executor.execute(
                            new Worker(iterations, i, rank, rows, cols, dim,
                                startLatch, endLatch, times));
                    }
                    endLatch.await();
                }
                mainTimer.stop();
                executor.shutdown();

            } else {
                System.out.println("HJ Threads");
                mainTimer.start();
                for (int loops = 0; loops < outerloops; ++loops) {
                    final CountDownLatch startLatch = new CountDownLatch(threadCount);
                    final CountDownLatch endLatch = new CountDownLatch(threadCount);
                    launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                        new Worker(iterations, threadIdx, rank, rows, cols, dim,
                            startLatch, endLatch, times).run();
                    }));
                    endLatch.await();
                }
                mainTimer.stop();
            }


        } else {

            MPI.COMM_WORLD.barrier();
            mainTimer.start();
            for (int loops = 0; loops < outerloops; ++loops) {
                mmManager(iterations, 0, rank, rows, cols, dim, times);
                MPI.COMM_WORLD.barrier();
            }
            mainTimer.stop();

            worldTimes  = new double[MPI.COMM_WORLD.getSize()];
            MPI.COMM_WORLD.gather(times, 1, MPI.DOUBLE, worldTimes, 1, MPI.DOUBLE, 0);
        }


        if (rank == 0){
            System.out.println(
                "" + mainTimer.elapsed(TimeUnit.MILLISECONDS) + "\t"
                + findMinMaxAvg(
                    (threadCount > 1 ? times : worldTimes), (threadCount > 1)));
        }


        MPI.Finalize();
    }

    private static String findMinMaxAvg(double[] array, boolean threads){
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double avg = 0.0;
        double t;
        for (int i = 0; i < array.length; ++i){
            t = array[i];
            if (t < min) min = t;
            if (t > max) max = t;
            avg+=t;
        }
//        return min +" " + max + " avg across " + (threads ? "threads " : "MPI ") + (avg/array.length);
        return min + "\t" + max + "\t" + (avg/array.length);
    }

    private static void mmManager(
        int iterations, int threadIdx, int rank, int rows, int cols, int dim,
        double[] times) {
        double [] A = new double[rows*cols];
        double [] B = new double[cols*dim];
        double [] C = new double[rows*dim];
        double [] Adiag = new double[rows];
        for (int i = 0; i < rows*cols; ++i){
            A[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
        }

        for (int i = 0; i < cols*dim; ++i){
            B[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
        }
        for (int i = 0; i < rows*dim; ++i){
            C[i] = 0.0;
        }

        for (int i = 0; i < rows; ++i){
            Adiag[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
        }

        Stopwatch mmTimer = Stopwatch.createUnstarted();
        double avgMMTime = 0.0;
        double maxMMTime = Double.MIN_VALUE;
        double minMMTime = Double.MAX_VALUE;
        double t;
        mmTimer.start();
        for (int i = 0; i < iterations; ++i){
            MatrixUtils.matrixMultiplyWithThreadOffset(A, Adiag, B, rows, dim, cols, 64, 0, 0, C);
        }

        mmTimer.stop();
        t = mmTimer.elapsed(TimeUnit.MILLISECONDS);
        mmTimer.reset();
        avgMMTime += t;
        if (maxMMTime < t) maxMMTime = t;
        if (minMMTime > t) minMMTime = t;
        times[0] += t;
//        System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);
    }
}
