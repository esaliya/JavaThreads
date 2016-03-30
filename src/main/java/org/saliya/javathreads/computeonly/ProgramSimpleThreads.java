package org.saliya.javathreads.computeonly;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Booleans;
import mpi.MPI;
import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class ProgramSimpleThreads {
    static class Worker implements Runnable{
        int iterations;
        int threadIdx;
        int rank;
        int rows;
        int cols;
        int dim;
        CountDownLatch startLatch;
        CountDownLatch endLatch;

        Worker (int iterations, int threadIdx, int rank, int rows, int cols, int dim, CountDownLatch startLatch, CountDownLatch endLatch){
            this.iterations = iterations;
            this.threadIdx = threadIdx;
            this.rank = rank;
            this.rows = rows;
            this.cols = cols;
            this.dim = dim;

            this.startLatch = startLatch;
            this.endLatch = endLatch;

        }
        @Override
        public void run() {

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

            endLatch.countDown();
            System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);

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

        int rank = MPI.COMM_WORLD.getRank();

        Stopwatch mainTimer = Stopwatch.createUnstarted();



        if (threadCount > 1){
            final CountDownLatch startLatch = new CountDownLatch(threadCount);
            final CountDownLatch endLatch = new CountDownLatch(threadCount);

            if (!hj) {
                System.out.println("Java Threads");
                mainTimer.start();
                for (int i = 0; i < threadCount; ++i) {
                    new Thread(new Worker(iterations, i, rank, rows, cols, dim,
                        startLatch, endLatch)).start();
                }
            } else {
                System.out.println("HJ Threads");
                mainTimer.start();
                launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                    new Worker(iterations, threadIdx, rank, rows, cols, dim,
                        startLatch, endLatch).run();
                }));
            }
            endLatch.await();
            mainTimer.stop();

        } else {
            MPI.COMM_WORLD.barrier();
            mainTimer.start();
            mmManager(iterations, 0, rank, rows, cols, dim);
            MPI.COMM_WORLD.barrier();
            mainTimer.stop();
        }


        if (rank == 0){
            System.out.println("Main Time: " + mainTimer.elapsed(TimeUnit.MILLISECONDS));
        }

        MPI.Finalize();
    }

    private static void mmManager(
        int iterations, int threadIdx, int rank, int rows, int cols, int dim) {
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
        System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);
    }
}
