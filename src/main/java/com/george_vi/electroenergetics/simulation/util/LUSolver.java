package com.george_vi.electroenergetics.simulation.util;

import java.util.Map;

public class LUSolver {

    // thank you, chat gpt

    public static double[] solve(SparseMatrix<Double> A, double[] b) {
        int n = b.length;
        double[][] denseA = toDense(A, n);

        int[] pivot = new int[n];
        for (int i = 0; i < n; i++) pivot[i] = i;

        for (int k = 0; k < n; k++) {
            int max = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(denseA[i][k]) > Math.abs(denseA[max][k])) {
                    max = i;
                }
            }

            if (Math.abs(denseA[max][k]) < 1e-12)
                return new double[b.length];

            double[] tempRow = denseA[k];
            denseA[k] = denseA[max];
            denseA[max] = tempRow;

            int tempPivot = pivot[k];
            pivot[k] = pivot[max];
            pivot[max] = tempPivot;

            for (int i = k + 1; i < n; i++) {
                denseA[i][k] /= denseA[k][k];
                for (int j = k + 1; j < n; j++) {
                    denseA[i][j] -= denseA[i][k] * denseA[k][j];
                }
            }
        }

        double[] pb = new double[n];
        for (int i = 0; i < n; i++) {
            pb[i] = b[pivot[i]];
        }

        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            y[i] = pb[i];
            for (int j = 0; j < i; j++) {
                y[i] -= denseA[i][j] * y[j];
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = y[i];
            for (int j = i + 1; j < n; j++) {
                x[i] -= denseA[i][j] * x[j];
            }
            x[i] /= denseA[i][i];
        }

        return x;
    }

    private static double[][] toDense(SparseMatrix<Double> sparse, int n) {
        double[][] dense = new double[n][n];
        for (int i = 0; i < n; i++) {
            Map<Integer, Double> row = sparse.getRow(i);
            if (row == null) continue;
            for (Map.Entry<Integer, Double> e : row.entrySet()) {
                int j = e.getKey();
                if (j >= 0 && j < n) {
                    dense[i][j] = e.getValue();
                }
            }
        }
        return dense;
    }
}
