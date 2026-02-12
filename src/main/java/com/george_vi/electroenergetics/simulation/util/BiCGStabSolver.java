package com.george_vi.electroenergetics.simulation.util;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public class BiCGStabSolver {
    private static final double eps = 1e-16d;

    public static double[] solve(SparseMatrix A, double[] b, double[] x, double tolerance, int maxIterations) {
        int n = A.size;
//        A.scale(b);

        double[] r = new double[n];
        double[] rHat = new double[n];
        double[] p = new double[n];
        double[] v = new double[n];
        double[] y = new double[n];
        double[] s = new double[n];
        double[] z = new double[n];
        double[] t = new double[n];

        A.computeResidualInto(x, b, r);
        System.arraycopy(r, 0, rHat, 0, n);

        double rhoOld = 1.0;
        double alpha = 1.0;
        double omega = 1.0;

        ILUPreconditioner ilu = ILUPreconditioner.computeILU0(A);
        int iter;
        for (iter = 0; iter < maxIterations; iter++) {
            double rho = VectorHelpers.dot(rHat, r);
            if (rho == 0.0)
                break;

            double beta = (rho / rhoOld) * (alpha / omega);

            for (int i = 0; i < n; i++)
                p[i] = r[i] + beta * (p[i] - omega * v[i]);

//            VectorHelpers.applyJacobiPreconditionerInto(A, p, y);
            ilu.applyPreconditioner(p, y);

            A.multiplyAndFillInto(y, v);

            double rHatv = iter == 0 ? 1e-4d : VectorHelpers.dot(rHat, v);
            if (rHatv == 0.0)
                break;
            alpha = rho / rHatv;

            for (int i = 0; i < n; i++)
                s[i] = r[i] - alpha * v[i];

            if (VectorHelpers.normSqr(s) < tolerance * tolerance) {
                for (int i = 0; i < n; i++)
                    x[i] += alpha * y[i];
                A.computeResidualInto(x, b, r);
                if (VectorHelpers.normSqr(r) < tolerance * tolerance)
                    break;
            }

//            VectorHelpers.applyJacobiPreconditionerInto(A, s, z);
            ilu.applyPreconditioner(s, z);

            A.multiplyAndFillInto(z, t);

            double ts = VectorHelpers.dot(t, s);
            double tt = VectorHelpers.dot(t, t);

            if (tt == 0.0)
                break;

            omega = ts / tt;

            for (int i = 0; i < n; i++)
                x[i] += alpha * y[i] + omega * z[i];

            for (int i = 0; i < n; i++)
                r[i] = s[i] - omega * t[i];

            if (VectorHelpers.normSqr(r) < tolerance * tolerance) {
                A.computeResidualInto(x, b, r);
                if (VectorHelpers.normSqr(r) < tolerance * tolerance)
                    break;
            }

            rhoOld = rho;
        }

//        A.unscaleResults(x);
        return x;
    }
}
