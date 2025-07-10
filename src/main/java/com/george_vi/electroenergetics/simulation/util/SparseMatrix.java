package com.george_vi.electroenergetics.simulation.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SparseMatrix<T> {
    Map<Integer, Map<Integer, T>> data = new HashMap<>();
    final T defaultValue;

    public SparseMatrix(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static SparseMatrix<Double> matrixD(){
        return new SparseMatrix<Double>(0.0d);
    }

    public void set(int row, int col, T value) {
        if (!data.containsKey(row))
            data.computeIfAbsent(row, k -> new HashMap<>()).put(col, value);
        else if (data.get(row).containsKey(col))
            data.get(row).replace(col, value);
        else
            data.get(row).put(col, value);
    }

    public T get(int row, int col) {
        return data.getOrDefault(row, Collections.emptyMap()).getOrDefault(col, defaultValue);
    }

    public int size() {
        return data.keySet().stream().mapToInt(i -> i).max().orElse(0) + 1;
    }

    public Map<Integer, T> getRow(int row) {
        return data.getOrDefault(row, Collections.emptyMap());
    }
}
