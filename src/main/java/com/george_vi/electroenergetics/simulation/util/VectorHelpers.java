package com.george_vi.electroenergetics.simulation.util;

public class VectorHelpers {
    public static double normSqr(double[] v) {
        double sum = 0.0;
        for (double value : v)
            sum += value * value;
        return sum;
    }

    public static double norm(double[] v) {
        double sum = 0.0;
        for (double value : v)
            sum += value * value;
        return Math.sqrt(sum);
    }

    public static void applyJacobiPreconditionerInto(SparseMatrix A, double[] v, double[] z) {
        for (int i = 0; i < v.length; i++) {
            double diagonal = A.getValue(i, i);
            if (diagonal == 0) diagonal = 1e-4d;
            z[i] = v[i] / diagonal;
        }
    }

    public static double dot(double[] a, double[] b) {
        double dot = 0d;
        for (int i = 0; i < a.length; i++)
            dot += a[i] * b[i];

        return dot;
    }

    public static double actualDot(double[] a, double[] b) {
        double sum = 0.0;
        double c = 0.0;
        for (int i = 0; i < a.length; i++) {
            double y = a[i] * b[i] - c;
            double t = sum + y;
            c = (t - sum) - y;
            sum = t;
        }
        return sum;
    }
}
