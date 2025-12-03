package com.george_vi.electroenergetics.simulation.util;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public class LUSolver {

    // thank you, ChatGPT
    // believe it or not, ChatGPT wrote this
    // I have no idea what this all even is, but it works and that's important

    public static double[] solve(double[][] A, double[] b) {
        int n = b.length;
        int[] pivot = new int[n];

        for (int i = 0; i < n; i++) pivot[i] = i;

        // Apply the same swaps to b that you apply to A
        for (int k = 0; k < n; k++) {

            // pivot search
            int max = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(A[i][k]) > Math.abs(A[max][k])) {
                    max = i;
                }
            }

            if (Math.abs(A[max][k]) < 1e-12)
                return new double[n];

            // swap rows A[k] <-> A[max]
            double[] tmp = A[k];
            A[k] = A[max];
            A[max] = tmp;

            // swap b[k] <-> b[max]
            double tb = b[k];
            b[k] = b[max];
            b[max] = tb;

            // swap pivot indices (optional but kept)
            int tp = pivot[k];
            pivot[k] = pivot[max];
            pivot[max] = tp;

            // elimination
            for (int i = k + 1; i < n; i++) {
                A[i][k] /= A[k][k];
                double m = A[i][k];
                for (int j = k + 1; j < n; j++) {
                    A[i][j] -= m * A[k][j];
                }
            }
        }

        // forward substitution (y = L⁻¹ b)
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = b[i];
            for (int j = 0; j < i; j++) {
                sum -= A[i][j] * y[j];
            }
            y[i] = sum;
        }

        // backward substitution (x = U⁻¹ y)
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * x[j];
            }
            x[i] = sum / A[i][i];
        }

        return x;
    }

    public static double[] solve(SparseMatrix A, double[] b) {
        int n = b.length;
        int[] pivot = new int[n];

        for (int i = 0; i < n; i++) pivot[i] = i;

        // Apply the same swaps to b that you apply to A
        for (int k = 0; k < n; k++) {

            // pivot search
            int max = k;
            double pivotMaxValue = Math.abs(A.getValue(k, k));
            for (int i = k + 1; i < n; i++) {
                double val = Math.abs(A.getValue(i, k));
                if (val > pivotMaxValue) {
                    max = i;
                    pivotMaxValue = val;
                }
            }

            if (pivotMaxValue < 1e-12)
                return new double[n];

            // swap rows A[k] <-> A[max]
            A.swapRows(k, max);

            // swap b[k] <-> b[max]
            double tb = b[k];
            b[k] = b[max];
            b[max] = tb;

            // swap pivot indices (optional but kept)
            int tp = pivot[k];
            pivot[k] = pivot[max];
            pivot[max] = tp;

            // elimination

            Int2DoubleMap rowK = A.getRow(k);
            for (int i = k + 1; i < n; i++) {
                double aik = A.getValue(i,k);
                if (aik == 0) continue;

                double m = aik / rowK.get(k);
                A.set(i,k, m);

                for (Int2DoubleMap.Entry e : rowK.int2DoubleEntrySet()) {
                    int j = e.getIntKey();
                    if (j <= k) continue;
                    double newValue = A.getValue(i,j) - m * e.getDoubleValue();
                    A.set(i,j, newValue);
                }
            }
        }

        // forward substitution (y = L⁻¹ b)
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = b[i];
            for (Int2DoubleMap.Entry e : A.getRow(i).int2DoubleEntrySet())
                if (e.getIntKey() < i)
                    sum -= e.getDoubleValue() * y[e.getIntKey()];

            y[i] = sum;
        }

        // backward substitution (x = U⁻¹ y)
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (Int2DoubleMap.Entry e : A.getRow(i).int2DoubleEntrySet())
                if (e.getIntKey() < n && e.getIntKey() > i)
                    sum -= e.getDoubleValue() * x[e.getIntKey()];

            x[i] = sum / A.getValue(i, i);
        }

        return x;
    }
}
