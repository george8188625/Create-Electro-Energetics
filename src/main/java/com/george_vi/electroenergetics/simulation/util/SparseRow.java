package com.george_vi.electroenergetics.simulation.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class SparseRow {
    double[] values;
    IntList nzCols;

    public SparseRow(int size) {
        values = new double[size];
        nzCols = new IntArrayList(8);
    }

    public void put(int col, double val) {
        if (val != 0 && values[col] == 0)
            nzCols.add(col);
        else if (val == 0 && values[col] != 0)
            nzCols.removeIf(v -> v == col);
        values[col] = val;
    }

    public double get(int col) {
        return values[col];
    }

    public IntList getNz() {
        return nzCols;
    }
}
