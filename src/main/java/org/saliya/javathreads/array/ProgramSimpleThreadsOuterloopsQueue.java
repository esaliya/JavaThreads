package org.saliya.javathreads.array;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import mpi.MPIException;
import net.openhft.affinity.Affinity;
import org.saliya.javathreads.MatrixUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import static edu.rice.hj.Module0.asyncPhased;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.forallChunked;

public class ProgramSimpleThreadsOuterloopsQueue {
    static boolean run;
    static class Work {
        CountDownLatch startLatch;
        CountDownLatch endLatch;
    }
    static class Worker implements Runnable{
        int iterations;
        int threadIdx;
        int rank;
        int rows;
        int cols;
        int dim;

        Queue<Work> works;

        Worker (int iterations, int threadIdx, int rank, int rows, int cols, int dim, Queue<Work> works){
            this.iterations = iterations;
            this.threadIdx = threadIdx;
            this.rank = rank;
            this.rows = rows;
            this.cols = cols;
            this.dim = dim;

            this.works = works;

        }
        @Override
        public void run() {
            while (run) {
                BitSet bitSet = new BitSet(48);
                bitSet.set(threadIdx + 1);
//            bitSet.set(threadIdx+1+24);
                Affinity.setAffinity(bitSet);

                // busy wait
                Work w = works.poll();
                if (w == null) {
                    continue;
                }

                CountDownLatch startLatch = w.startLatch;
                CountDownLatch endLatch = w.endLatch;

                double[] A = new double[rows * cols];
                double[] B = new double[cols * dim];
                double[] C = new double[rows * dim];
                double[] Adiag = new double[rows];
                for (int i = 0; i < rows * cols; ++i) {
                    A[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
                }

                for (int i = 0; i < cols * dim; ++i) {
                    B[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
                }
                for (int i = 0; i < rows * dim; ++i) {
                    C[i] = 0.0;
                }

                for (int i = 0; i < rows; ++i) {
                    Adiag[i] = (i & 1) == 0 ? (0.9999995 / 1.0000023) : (1.0000023 / 0.9999995);
                }

                startLatch.countDown();
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Stopwatch mmTimer = Stopwatch.createUnstarted();
                double avgMMTime = 0.0;
                double maxMMTime = Double.MIN_VALUE;
                double minMMTime = Double.MAX_VALUE;
                double t;
                mmTimer.start();
                for (int i = 0; i < iterations; ++i) {
                    MatrixUtils.matrixMultiplyWithThreadOffset(A, Adiag, B, rows, dim, cols, 64, 0, 0, C);
                }

                mmTimer.stop();
                t = mmTimer.elapsed(TimeUnit.MILLISECONDS);
                mmTimer.reset();
                avgMMTime += t;
                if (maxMMTime < t) maxMMTime = t;
                if (minMMTime > t) minMMTime = t;

                endLatch.countDown();
//            System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);
            }
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

        Stopwatch mainTimer = Stopwatch.createUnstarted();


        if (threadCount > 1){
            if (!hj) {
                System.out.println("Java Threads");
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                mainTimer.start();
                List<Queue<Work>> queueList = new ArrayList<>(threadCount);
                // start the threads
                for (int i = 0; i < threadCount; ++i) {
                    Queue<Work> workQueue = new ConcurrentLinkedQueue<>();
                    queueList.add(i, workQueue);
                    executor.execute(
                            new Worker(iterations, i, rank, rows, cols, dim, workQueue));
                }

                for (int loops = 0; loops < outerloops; ++loops) {
                    final CountDownLatch startLatch = new CountDownLatch(threadCount);
                    final CountDownLatch endLatch = new CountDownLatch(threadCount);

                    // now put the work in to queue
                    for (int i = 0; i < threadCount; ++i) {
                        Work w = new Work();
                        w.endLatch = endLatch;
                        w.startLatch = startLatch;

                        Queue<Work> workQueue = queueList.get(i);
                        workQueue.add(w);
                    }
                    endLatch.await();
                }
                mainTimer.stop();
                executor.shutdown();
            } else {
//                System.out.println("HJ Threads");
//                mainTimer.start();
//                for (int loops = 0; loops < outerloops; ++loops) {
//                    final CountDownLatch startLatch = new CountDownLatch(threadCount);
//                    final CountDownLatch endLatch = new CountDownLatch(threadCount);
//                    launchHabaneroApp(() -> forallChunked(0, threadCount - 1, (threadIdx) -> {
//                        new Worker(iterations, threadIdx, rank, rows, cols, dim,
//                            startLatch, endLatch).run();
//                    }));
//                    endLatch.await();
//                }
//                mainTimer.stop();
            }
        } else {

            MPI.COMM_WORLD.barrier();
            mainTimer.start();
            for (int loops = 0; loops < outerloops; ++loops) {
                mmManager(iterations, 0, rank, rows, cols, dim);
                MPI.COMM_WORLD.barrier();
            }
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
//        System.out.println(rank  + "\t" + threadIdx + "\t" + (avgMMTime) + "\t" + minMMTime + "\t" + maxMMTime);
    }
}
