package com.george_vi.electroenergetics.simulation.util;

public record ILUPreconditioner(SparseMatrix L, SparseMatrix U) {

    public void applyPreconditioner(double[] r, double[] z) {
            int n = r.length;
            double[] y = new double[n];

            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j : L.data[i].getNz()) {
                    if (j >= i) continue;
                    sum += L.getValue(i, j) * y[j];
                }
                y[i] = r[i] - sum;
            }

            for (int i = n - 1; i >= 0; i--) {
                double sum = 0.0;
                for (int j : U.data[i].getNz()) {
                    if (j <= i) continue;
                    sum += U.getValue(i, j) * z[j];
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
            for (int j : A.data[i].getNz()) {
                double Aij = A.getValue(i, j);

                double sum = 0.0;
                for (int k : L.data[i].getNz()) {
                    if (k >= j) continue;
                    double Lik = L.getValue(i, k);
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

