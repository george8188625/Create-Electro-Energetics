package com.george_vi.electroenergetics.simulation.util;

public class LUSolver {

    // thank you, ChatGPT
    // believe it or not, ChatGPT wrote this
    // I have no idea what this all even is, but it works and that's important

    public static double[] solve(double[][] denseA, double[] b) {
        int n = b.length;

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
}
