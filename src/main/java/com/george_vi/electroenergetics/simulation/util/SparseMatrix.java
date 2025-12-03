package com.george_vi.electroenergetics.simulation.util;

import it.unimi.dsi.fastutil.ints.*;

public class SparseMatrix {
    public final int size;
    Int2DoubleMap[] data;
    public SparseMatrix(int size) {
        this.size = size;
        data = new Int2DoubleMap[size];
    }

    public Int2DoubleMap getRow(int row) {
        return data[row] == null ? Int2DoubleMaps.EMPTY_MAP : data[row];
    }

    public double getValue(int row, int col) {
        return data[row] == null ? 0d : data[row].get(col);
    }

    public void set(int row, int col, double val) {
        if (data[row] == null)
            data[row] = new Int2DoubleOpenHashMap();
        data[row].put(col, val);
    }

    public void multiplyAndFillInto(double[] vector, double[] toFill) {
        for (int i = 0; i < size; i++) {
            double sum = 0;
            if (data[i] != null)
                for (Int2DoubleMap.Entry e : data[i].int2DoubleEntrySet())
                    sum += e.getDoubleValue() * vector[e.getIntKey()];
            toFill[i] = sum;
        }
    }

    public void computeResidualInto(double[] x, double[] b, double[] toFill) {
        for (int i = 0; i < size; i++) {
            double sum = 0;
            if (data[i] != null)
                for (Int2DoubleMap.Entry e : data[i].int2DoubleEntrySet())
                    sum += e.getDoubleValue() * x[e.getIntKey()];

            toFill[i] = b[i] - sum;
        }
    }

    public void swapRows(int row1, int row2) {
        Int2DoubleMap a = data[row1];
        data[row1] = data[row2];
        data[row2] = a;
    }

    public void swapRowsPartial(int row1, int row2, int maxColumn) {
        Int2DoubleMap map1 = data[row1];
        Int2DoubleMap map2 = data[row2];

        for (int col = 0; col < maxColumn; col++) {
            double val1 = map1.getOrDefault(col, 0.0);
            double val2 = map2.getOrDefault(col, 0.0);

            if (val2 == 0.0) map1.remove(col); else map1.put(col, val2);
            if (val1 == 0.0) map2.remove(col); else map2.put(col, val1);
        }
    }

    double[] Dr;
    double[] Dc;

    public void scale(double[] rhs) {
        Dr = new double[size];

        for (int i = 0; i < size; i++) {
            Int2DoubleMap row = data[i];
            double max = 0.0;

            if (row != null) {
                for (Int2DoubleMap.Entry e : row.int2DoubleEntrySet()) {
                    double v = Math.abs(e.getDoubleValue());
                    if (v > max) max = v;
                }
            }

            if (max == 0.0) {
                Dr[i] = 1.0;   // empty row — do nothing
                continue;
            }

            Dr[i] = 1.0 / max;

            // scale matrix row
            for (Int2DoubleMap.Entry e : row.int2DoubleEntrySet()) {
                row.put(e.getIntKey(), e.getDoubleValue() * Dr[i]);
            }

            // scale RHS
            rhs[i] *= Dr[i];
        }

        Dc = new double[size];

        // For each column, find its max
        for (int col = 0; col < size; col++) {
            double max = 0.0;

            // scan column by scanning each row
            for (int row = 0; row < size; row++) {
                Int2DoubleMap r = data[row];
                if (r == null) continue;

                double val = r.getOrDefault(col, 0.0);
                double a = Math.abs(val);
                if (a > max) max = a;
            }

            if (max == 0.0) {
                Dc[col] = 1.0;
                continue;
            }

            Dc[col] = 1.0 / max;

            // now scale that column
            for (int row = 0; row < size; row++) {
                Int2DoubleMap r = data[row];
                if (r == null) continue;

                if (r.containsKey(col))
                    r.put(col, r.get(col) * Dc[col]);
            }
        }
    }

    public void unscaleResults(double[] x) {
        for (int j = 0; j < size; j++)
            x[j] *= Dc[j];
    }
}
