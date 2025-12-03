package com.george_vi.electroenergetics.simulation.util;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public record ILUPreconditioner(SparseMatrix L, SparseMatrix U) {

    public void applyPreconditioner(double[] r, double[] z) {
            int n = r.length;
            double[] y = new double[n];

            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (Int2DoubleMap.Entry e : this.L.getRow(i).int2DoubleEntrySet()) {
                    int j = e.getIntKey();
                    if (j >= i) continue;
                    sum += e.getDoubleValue() * y[j];
                }
                y[i] = r[i] - sum;
            }

            for (int i = n - 1; i >= 0; i--) {
                double sum = 0.0;
                for (Int2DoubleMap.Entry e : this.U.getRow(i).int2DoubleEntrySet()) {
                    int j = e.getIntKey();
                    if (j <= i) continue;
                    sum += e.getDoubleValue() * z[j];
                }
                double Uii = this.U.getValue(i, i);
                if (Uii == 0) Uii = 1.0;
                z[i] = (y[i] - sum) / Uii;
            }
        }

    public static ILUPreconditioner computeILU0(SparseMatrix A) {
        int n = A.size;
        SparseMatrix L = new SparseMatrix(n);
        SparseMatrix U = new SparseMatrix(n);

        for (int i = 0; i < n; i++) {
            for (Int2DoubleMap.Entry entry : A.getRow(i).int2DoubleEntrySet()) {
                int j = entry.getIntKey();
                double Aij = entry.getDoubleValue();

                double sum = 0.0;
                for (Int2DoubleMap.Entry eL : L.getRow(i).int2DoubleEntrySet()) {
                    int k = eL.getIntKey();
                    if (k >= j) continue;
                    double Lik = eL.getDoubleValue();
                    double Ukj = U.getValue(k, j);
                    sum += Lik * Ukj;
                }

                if (j < i) {
                    if (U.getValue(j, j) == 0) U.set(j, j, 1.0);
                    L.set(i, j, (Aij - sum) / U.getValue(j, j));
                } else {
                    U.set(i, j, Aij - sum);
                    if (i == j) L.set(i, i, 1.0);
                }
            }
        }

        return new ILUPreconditioner(L, U);
    }
}

