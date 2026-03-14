package com.george_vi.electroenergetics.simulation.util;


import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class CholeskySolver {

    public static double[] solve(SparseMatrix A, double[] b) {

        int n = b.length;
        IntList[] colNz = new IntList[n];
        for (int i = 0; i < n; i++) colNz[i] = new IntArrayList();

        for (int j = 0; j < n; j++) {
            SparseRow rowJ = A.data[j];
            double sum = rowJ.get(j);
            for (int k : rowJ.getNz()) {
                if (k >= j) continue;
                sum -= rowJ.values[k] * rowJ.values[k];
            }

            if (sum <= 0)
                return new double[n];
            double ljj = Math.sqrt(sum);
            rowJ.put(j, ljj);

            for (int i = j + 1; i < n; i++) {
                SparseRow rowI = A.data[i];
                double s = rowI.get(j);
                for (int k : rowI.getNz()) {
                    if (k >= j) continue;
                    s -= rowI.values[k] * rowJ.values[k];
                }
                double lij = s / ljj;
                if (lij != 0) {
                    rowI.put(j, lij);
                    colNz[j].add(i);
                }

            }
        }

        // forward substitution (Ly = b)
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            SparseRow rowI = A.data[i];
            double sum = b[i];
            for (int col : rowI.getNz()) {
                if (col >= i) continue;
                sum -= rowI.values[col] * y[col];
            }

            y[i] = sum / A.getValue(i, i);
        }

        // backward substitution (Lᵀx = y)
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (int k : colNz[i]) {
                if (k <= i) continue;
                sum -= A.getValue(k, i) * x[k];
            }

            x[i] = sum / A.getValue(i, i);
        }
        return x;
    }
}
