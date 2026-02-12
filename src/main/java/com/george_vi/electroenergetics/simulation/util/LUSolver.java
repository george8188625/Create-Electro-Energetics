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
        double[] tempIK = new double[n];
        for (int i = 0; i < n; i++) pivot[i] = i;

        for (int k = 0; k < n; k++) {

            int max = k;
            double pivotMaxValue = Math.abs(A.getValue(k, k));
            for (int i = k + 1; i < n; i++) {
                double v = A.getValue(i, k);
                tempIK[i] = v;
                double val = Math.abs(v);
                if (val > pivotMaxValue) {
                    max = i;
                    pivotMaxValue = val;
                }
            }

            if (pivotMaxValue < 1e-12)
                return new double[n];
            if (k != max)
                A.swapRows(k, max);


            double tb = b[k];
            b[k] = b[max];
            b[max] = tb;

            int tp = pivot[k];
            pivot[k] = pivot[max];
            pivot[max] = tp;

            SparseRow rowK = A.data[k];
            for (int i = k + 1; i < n; i++) {
                SparseRow rowI = A.data[i];
                double aik = k == max ? tempIK[i] : rowI.values[k];
                if (aik == 0) continue;

                double m = aik / rowK.values[k];
                rowI.put(k, m);

                for (int j : rowK.getNz()) {
                    if (j <= k) continue;
                    double newValue = rowI.values[j] - m * rowK.values[j];
                    rowI.put(j, newValue);
                }
            }
        }

        // forward substitution (y = L⁻¹ b)
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            SparseRow rowI = A.data[i];
            double sum = b[i];
            for (int col : rowI.getNz())
                if (col < i)
                    sum -= rowI.values[col] * y[col];

            y[i] = sum;
        }

        // backward substitution (x = U⁻¹ y)
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            SparseRow rowI = A.data[i];
            double sum = y[i];
            for (int col : rowI.getNz())
                if (col < n && col > i)
                    sum -= rowI.values[col] * x[col];

            x[i] = sum / rowI.values[i];
        }

        return x;
    }
}
