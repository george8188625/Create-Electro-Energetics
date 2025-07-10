package com.george_vi.electroenergetics.simulation.util;

import java.util.Map;

public class LUSolver {
//    public static double[] solve(SparseMatrix<Double> a, double[] b, double tolerance, int maxIterations) {
//        int n = b.length;
//
//        double[] x = new double[n];
//        double[] r = subtract(b, multiply(a, x));
//        double[] p = r.clone();
//        double rsOld = dot(r, r);
//
//        for (int i = 0; i < maxIterations; i++) {
//            double[] ap = multiply(a, p);
//            double dotProduct = dot(p, ap);
//
//            if (Math.abs(dotProduct) < 1e-10)
//                return x;
//
//            double alpha = rsOld / dotProduct;
//
//            for (int j = 0; j < n; j++) {
//                x[j] += alpha * p[j];
//                r[j] -= alpha * ap[j];
//            }
//
//            double rsNew = dot(r, r);
//            if (Math.sqrt(rsNew) < tolerance)
//                break;
//
//            double beta = rsNew / rsOld;
//            for (int j = 0; j < n; j++) {
//                p[j] = r[j] + beta * p[j];
//            }
//
//            rsOld = rsNew;
//        }
//
//        return x;
//    }
//
//    static double[] multiply(SparseMatrix<Double> a, double[] x) {
//        int n = x.length;
//        double[] result = new double[n];
//
//        for (int i = 0; i < n; i++) {
//            double sum = 0.0;
//            for (int j = 0; j < n; j++) {
//                Double value = a.get(i, j);
//                if (value != null && value != 0.0)
//                    sum += value * x[j];
//            }
//            result[i] = sum;
//        }
//
//        return result;
//    }
//
//
//    static double dot(double[] a, double[] b) {
//        double dot = 0.0;
//        for (int i = 0; i < a.length; i++)
//            dot += a[i] * b[i];
//
//        return dot;
//    }
//
//    static double[] subtract(double[] a, double[] b) {
//        double[] result = new double[a.length];
//        for (int i = 0; i < a.length; i++)
//            result[i] = a[i] - b[i];
//
//        return result;
//    }
//
//    static double[] add(double[] a, double[] b) {
//        double[] result = new double[a.length];
//        for (int i = 0; i < a.length; i++)
//            result[i] = a[i] + b[i];
//
//        return result;
//    }

    public static double[] solve(SparseMatrix<Double> A, double[] b) {
        int n = b.length;
        double[][] denseA = toDense(A, n);

        int[] pivot = new int[n];
        for (int i = 0; i < n; i++) pivot[i] = i;

        // LU Decomposition with partial pivoting
        for (int k = 0; k < n; k++) {
            int max = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(denseA[i][k]) > Math.abs(denseA[max][k])) {
                    max = i;
                }
            }

            if (Math.abs(denseA[max][k]) < 1e-12)
                return new double[b.length];

            // Swap rows
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

        // Apply permutation to b
        double[] pb = new double[n];
        for (int i = 0; i < n; i++) {
            pb[i] = b[pivot[i]];
        }

        // Forward substitution (Ly = Pb)
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            y[i] = pb[i];
            for (int j = 0; j < i; j++) {
                y[i] -= denseA[i][j] * y[j];
            }
        }

        // Backward substitution (Ux = y)
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
