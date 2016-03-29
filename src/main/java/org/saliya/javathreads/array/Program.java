package org.saliya.javathreads.array;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class Program {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);
        int rows = Integer.parseInt(args[2]);
        int cols = Integer.parseInt(args[3]);
        int dim = Integer.parseInt(args[4]);

        int rank = MPI.COMM_WORLD.getRank();
        Stopwatch mainTimer = Stopwatch.createUnstarted();
        MPI.COMM_WORLD.barrier();
        mainTimer.start();
        if (threadCount > 1){
            final CountDownLatch latch = new CountDownLatch(threadCount);
            launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                mmManager(iterations, threadIdx, rank, rows, cols, dim, latch);
            }));
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            mmManager(iterations, 0, rank, rows, cols, dim, latch);
        }
        MPI.COMM_WORLD.barrier();
        mainTimer.stop();
        if (rank == 0){
            System.out.println("Main Time: " + mainTimer.elapsed(TimeUnit.MILLISECONDS));
        }

        MPI.Finalize();
    }

    private static void mmManager(
        int iterations, int threadIdx, int rank, int rows, int cols, int dim,
        CountDownLatch latch) {
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

        latch.countDown();

        Stopwatch mmTimer = Stopwatch.createUnstarted();
        double avgMMTime = 0.0;
        double maxMMTime = Double.MIN_VALUE;
        double minMMTime = Double.MAX_VALUE;
        double t;
        for (int i = 0; i < iterations; ++i){
            mmTimer.start();
            MatrixUtils.matrixMultiplyWithThreadOffset(A, Adiag, B, rows, dim, cols, 64, 0, 0, C);
            mmTimer.stop();
            t = mmTimer.elapsed(TimeUnit.MILLISECONDS);
            mmTimer.reset();
            avgMMTime += t;
            if (maxMMTime < t) maxMMTime = t;
            if (minMMTime > t) minMMTime = t;
        }
        System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime / iterations) + "\t" + minMMTime + "\t" + maxMMTime);
    }
}
