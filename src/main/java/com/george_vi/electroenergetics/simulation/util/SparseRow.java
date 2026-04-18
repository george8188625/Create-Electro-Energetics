package com.george_vi.electroenergetics.simulation.util;

public class SparseRow {
    double[] values;
    private int[] nzCols;
    private int nzColsSize;

    public SparseRow(int size) {
        values = new double[size];
        nzCols = new int[8];
        nzColsSize = 0;
    }

    public void put(int col, double val) {
        if (val != 0 && values[col] == 0)
            addToNZ(col);
        else if (val == 0 && values[col] != 0)
            removeNZ(col);
        values[col] = val;
    }

    private void addToNZ(int col) {
        if (nzColsSize == nzCols.length) {
            // Grow
            int[] old = nzCols;
            nzCols = new int[nzColsSize + 8];
            System.arraycopy(old, 0, nzCols, 0, nzColsSize);
        }

        nzCols[nzColsSize++] = col;
    }

    private void removeNZ(int col) {
        // Swap with last + remove
        for (int i = 0; i < nzColsSize; i++) {
            if (nzCols[i] == col) {
                nzCols[i] = nzCols[--nzColsSize];
                break;
            }
        }
    }

    public double get(int col) {
        return values[col];
    }

    public int[] getNz() {
        return java.util.Arrays.copyOf(nzCols, nzColsSize);
    }

    public int[] getFlatNz() {
        return nzCols;
    }

    public int getFlatNzSize() {
        return nzColsSize;
    }
}
