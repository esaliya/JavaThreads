package org.saliya.javathreads.letompparjava;

import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;

public class LetOmpPar {
    static double[] threadPartialBofZ;
    static double[] preX;
    static double[] threadPartialOutMM;
    static int targetDimension = 3;
    static int blockSize = 64;

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        if (args.length < 4) {
            System.out.println("Arguments: threadCount iterations rowCountPerUnit colCount");
            System.exit(1);
        }

        int rank = MPI.COMM_WORLD.getRank();

        int t = 0;
        int i = 0;
        int c = 0;
        int r = 0;

        t = Integer.parseInt(args[1]);
        i = Integer.parseInt(args[2]);
        r = Integer.parseInt(args[3]);
        c = Integer.parseInt(args[4]);

        /*bcReplica(t, i, c, r);*/

        MPI.Finalize();
    }

    static void matrixMultiply(double[] A, double[] B, int aHeight, int bWidth, int comm, int bz, double[] C, int threadAOffset, int threadCOffset) {

        int aHeightBlocks = aHeight / bz; // size = Height of A
        int aLastBlockHeight = aHeight - (aHeightBlocks * bz);
        if (aLastBlockHeight > 0) {
            aHeightBlocks++;
        }

        int bWidthBlocks = bWidth / bz; // size = Width of B
        int bLastBlockWidth = bWidth - (bWidthBlocks * bz);
        if (bLastBlockWidth > 0) {
            bWidthBlocks++;
        }

        int commnBlocks = comm / bz; // size = Width of A or Height of B
        int commLastBlockWidth = comm - (commnBlocks * bz);
        if (commLastBlockWidth > 0) {
            commnBlocks++;
        }

        int aBlockHeight = bz;
        int bBlockWidth;
        int commBlockWidth;

        int ib, jb, kb, i, j, k;
        int iARowOffset, kBRowOffset, iCRowOffset;
        for (ib = 0; ib < aHeightBlocks; ib++) {
            if (aLastBlockHeight > 0 && ib == (aHeightBlocks - 1)) {
                aBlockHeight = aLastBlockHeight;
            }
            bBlockWidth = bz;
            for (jb = 0; jb < bWidthBlocks; jb++) {
                if (bLastBlockWidth > 0 && jb == (bWidthBlocks - 1)) {
                    bBlockWidth = bLastBlockWidth;
                }
                commBlockWidth = bz;
                for (kb = 0; kb < commnBlocks; kb++) {
                    if (commLastBlockWidth > 0 && kb == (commnBlocks - 1)) {
                        commBlockWidth = commLastBlockWidth;
                    }

                    for (i = ib * bz; i < (ib * bz) + aBlockHeight; i++) {
                        iARowOffset = i*comm + threadAOffset;
                        iCRowOffset = i*bWidth + threadCOffset;
                        for (j = jb * bz; j < (jb * bz) + bBlockWidth;
                             j++) {
                            for (k = kb * bz;
                                 k < (kb * bz) + commBlockWidth; k++) {
                                kBRowOffset = k*bWidth;
                                if (A[iARowOffset + k] != 0 && B[kBRowOffset + j] != 0) {
                                    C[iCRowOffset + j] += A[iARowOffset + k] * B[kBRowOffset + j];
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*static void bcReplica(int threadCount, int iterations, int globalColCount, int rowCountPerUnit) {
        int pointComponentCountGlobal = globalColCount * targetDimension;
        int pointComponentCountLocal = rowCountPerUnit * targetDimension;
        preX = new double[pointComponentCountGlobal];
        int i;

        int pairCountLocal = rowCountPerUnit * globalColCount;
        threadPartialBofZ = new double[threadCount * pairCountLocal];
        threadPartialOutMM = new double[threadCount*pointComponentCountLocal];
        int j;


        double totalTime = 0.0;

        int itr;
        int k;

        double[] threadTimes = new double[threadCount];
        for (i = 0; i < threadCount; ++i) {
            threadTimes[i] = 0.0;
        }

        double v = 0.0;
        for (itr = 0; itr < iterations; ++itr) {
            for (i = 0; i < pointComponentCountGlobal; ++i) {
                preX[i] = Math.random();
            }

            int pairCountAllThreads = threadCount*pairCountLocal;
            for (k = 0; k < pairCountAllThreads; ++k) {
                threadPartialBofZ[k] = Math.random();
            }

            int pointComponentCountAllThreads = threadCount*pointComponentCountLocal;
            for (k = 0; k < pointComponentCountAllThreads; ++k) {
                threadPartialOutMM[k] = (double)0.0;
            }

            omp_set_num_threads(threadCount);

            #pragma omp parallel
            {
                int num_t = omp_get_num_threads();
                int tid = omp_get_thread_num();

                auto t = high_resolution_clock::now();
                matrixMultiply(threadPartialBofZ, preX, rowCountPerUnit, targetDimension, globalColCount, blockSize, threadPartialOutMM, tid*pairCountLocal, tid*pointComponentCountLocal);
                threadTimes[tid] += duration_cast<milliseconds>(high_resolution_clock::now() - t).count();
            }
        }

        int rank;
        int size;
        MPI_Comm_rank(MPI_COMM_WORLD, &rank);
        MPI_Comm_size(MPI_COMM_WORLD, &size);

        for (i = 0; i < threadCount; ++i)
        {
            threadTimes[i] = threadTimes[i] / iterations;
        }

        double* timeDistribution = (double*)(malloc(sizeof(double)*size*threadCount));
        MPI_Gather(threadTimes, threadCount, MPI_DOUBLE, timeDistribution, threadCount, MPI_DOUBLE, 0, MPI_COMM_WORLD);

        if (rank == 0) {
            for (i = 0; i < size*threadCount; ++i) {
                cout << "rank: " + to_string((long long)(i / threadCount)) + " tid: " + to_string((long long)(i % threadCount)) + " time: " + to_string((long double)timeDistribution[i]) + "\n";
            }
        }



    }*/

}
