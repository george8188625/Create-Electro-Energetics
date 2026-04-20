package com.george_vi.electroenergetics.simulation.util;


public class LUSolver {

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

                for (int jIndex = 0; jIndex < rowK.getFlatNzSize(); jIndex++) {
                    int j = rowK.getFlatNz()[jIndex];
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
            for (int colIndex = 0; colIndex < rowI.getFlatNzSize(); colIndex++) {
                int col = rowI.getFlatNz()[colIndex];
                if (col < i)
                    sum -= rowI.values[col] * y[col];
            }
            y[i] = sum;
        }

        // backward substitution (x = U⁻¹ y)
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            SparseRow rowI = A.data[i];
            double sum = y[i];
            for (int colIndex = 0; colIndex < rowI.getFlatNzSize(); colIndex++) {
                int col = rowI.getFlatNz()[colIndex];
                if (col < n && col > i)
                    sum -= rowI.values[col] * x[col];
            }

            x[i] = sum / rowI.values[i];
        }

        return x;
    }
}
