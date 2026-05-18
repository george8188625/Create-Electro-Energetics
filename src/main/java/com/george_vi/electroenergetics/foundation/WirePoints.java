package com.george_vi.electroenergetics.foundation;

import net.minecraft.world.phys.Vec3;

public class WirePoints {
    private double[] values;
    private int size;

    public Vec3 get(int index) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        return new Vec3(values[i], values[i + 1], values[i + 2]);
    }

    public double getX(int index) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        return values[i];
    }

    public double getY(int index) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        return values[i + 1];
    }

    public double getZ(int index) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        return values[i + 2];
    }

    public void copyInto(int index, double[] toFill, int toFillPos) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        System.arraycopy(values, i, toFill, toFillPos, 3);
    }

    public void add(double x, double y, double z) {
        int i = size * 3;
        if (values.length >= i)
            grow();
        values[i] = x;
        values[i + 1] = y;
        values[i + 2] = z;
        size++;
    }

    public void set(int index, double x, double y, double z) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        values[i] = x;
        values[i + 1] = y;
        values[i + 2] = z;
        size++;
    }

    private void grow() {
        double[] arr = new double[values.length + 3];
        System.arraycopy(values, 0, arr, 0, values.length);
        values = arr;
    }

    public void copyInto(int index, int positions, double[] toFill, int toFillPos) {
        if ((index + positions - 1) >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index + positions - 1);

        int i = index * 3;
        System.arraycopy(values, i, toFill, toFillPos, positions * 3);
    }

    public int size() {
        return size;
    }

}
