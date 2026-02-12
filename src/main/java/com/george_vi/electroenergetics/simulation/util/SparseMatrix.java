package com.george_vi.electroenergetics.simulation.util;

public class SparseMatrix {
    public final int size;
    public SparseRow[] data;
    public SparseMatrix(int size) {
        this.size = size;
        data = new SparseRow[size];
        for (int i = 0; i < size; i++) {
            data[i] = new SparseRow(size);
        }
    }

    public double getValue(int row, int col) {
        return data[row].values[col];
    }

    public void set(int row, int col, double val) {
        data[row].put(col, val);
    }

    public void multiplyAndFillInto(double[] vector, double[] toFill) {
        for (int i = 0; i < size; i++) {
            double sum = 0;
            for (int j : data[i].getNz())
                sum += data[i].get(j) * vector[j];
            toFill[i] = sum;
        }
    }

    public void computeResidualInto(double[] x, double[] b, double[] toFill) {
        for (int i = 0; i < size; i++) {
            double sum = 0;
            for (int j : data[i].getNz())
                sum += data[i].get(j) * x[j];

            toFill[i] = b[i] - sum;
        }
    }

    public void swapRows(int row1, int row2) {
        SparseRow a = data[row1];
        data[row1] = data[row2];
        data[row2] = a;
    }
}
