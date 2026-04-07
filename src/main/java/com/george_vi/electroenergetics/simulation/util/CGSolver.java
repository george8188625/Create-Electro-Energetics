package com.george_vi.electroenergetics.simulation.util;

public class CGSolver {
    // Actually works??

    private static final double eps = 1e-16d;
    public static double[] solve(SparseMatrix A, double[] b, double[] initialGuess, double tolerance, int maxIterations) {
        int n = A.size;
        double[] x = initialGuess;
        double[] res = new double[n];
        double[] z = new double[n];
        double[] p = new double[n];
        double[] Ap = new double[n];
        for (int i = 0; i < n; i++) {
//            double sum = 0;
//            for (Int2DoubleMap.Entry e : A.getRow(i).int2DoubleEntrySet())
//                sum += e.getDoubleValue() * x[e.getIntKey()];

//            res[i] = b[i] - sum;
        }
//        ILUPreconditioner ilu = ILUPreconditioner.computeILU0(A);
//        ilu.applyPreconditioner(res, z);
        VectorHelpers.applyJacobiPreconditionerInto(A, res, z);
        System.arraycopy(z, 0, p, 0, n);

        double rzOld = VectorHelpers.dot(res, z);

        int k;
        for (k = 0; k < maxIterations - 1; k++) {
            A.multiplyAndFillInto(p, Ap);

            double dot = VectorHelpers.dot(p, Ap);
            if (Math.abs(dot) < eps)
                break;

            double alpha = rzOld / dot;

            for (int i = 0; i < n; i++)
                x[i] += alpha * p[i];

            if (k != 0 && (k & 0b1111) == 0) {
//                for (int i = 0; i < n; i++) {
//                    double sum = 0;
//                    for (Int2DoubleMap.Entry e : A.getRow(i).int2DoubleEntrySet())
//                        sum += e.getDoubleValue() * x[e.getIntKey()];
//
//                    res[i] = b[i] - sum;
//                }
            }
            else for (int i = 0; i < n; i++)
                res[i] -= alpha * Ap[i];

            if (VectorHelpers.normSqr(res) < tolerance * tolerance)
                break;

            VectorHelpers.applyJacobiPreconditionerInto(A, res, z);
//            ilu.applyPreconditioner(res, z);
            double rzNew = VectorHelpers.dot(res, z);
            double beta = rzNew / rzOld;

            for (int i = 0; i < n; i++)
                p[i] = z[i] + beta * p[i];

            rzOld = rzNew;
        }
        return x;
    }

}
