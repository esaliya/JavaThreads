package org.saliya.javathreads.array;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import org.saliya.javathreads.MatrixUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class ProgramGlobalArray {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int threadCount = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);
        int rows = Integer.parseInt(args[2]);
        int cols = Integer.parseInt(args[3]);
        int dim = Integer.parseInt(args[4]);

        int rank = MPI.COMM_WORLD.getRank();

        double [] A = new double[threadCount*rows*cols];
        double [] B = new double[cols*dim];
        double [][] C = new double[threadCount][rows*dim];
        double [][] Adiag = new double[threadCount][rows];
        for (int i = 0; i < threadCount*rows*cols; ++i){
            A[i] = Math.random();
        }

        for (int i = 0; i < cols*dim; ++i){
            B[i] = Math.random();
        }
        for (int i = 0; i < threadCount; ++i) {
            for (int j = 0; j < rows * dim; ++j) {
                C[i][j] = 0.0;
            }
        }

        for (int i = 0; i < threadCount; ++i) {
            for (int j = 0; j < rows; ++j) {
                Adiag[i][j] = Math.random();
            }
        }


        if (threadCount > 1){
            final CountDownLatch latch = new CountDownLatch(threadCount);
            launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
                mmManager(iterations, threadIdx, rank, rows, cols, dim, A, Adiag[threadIdx], B, C[threadIdx], latch);
            }));
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            mmManager(iterations, 0, rank, rows, cols, dim, A, Adiag[0], B, C[0],
                latch);
        }

        MPI.Finalize();
    }

    private static void mmManager(
        int iterations, int threadIdx, int rank, int rows, int cols, int dim,
        double[] A, double[] Adiag, double[] B, double[] C, CountDownLatch latch) {

        latch.countDown();

        Stopwatch mmTimer = Stopwatch.createUnstarted();
        double avgMMTime = 0.0;
        double maxMMTime = Double.MIN_VALUE;
        double minMMTime = Double.MAX_VALUE;
        double t;
        for (int i = 0; i < iterations; ++i){
            mmTimer.start();
            MatrixUtils.matrixMultiplyWithThreadOffset(A, Adiag, B, rows, dim, cols, 64, threadIdx*rows, 0, C);
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
