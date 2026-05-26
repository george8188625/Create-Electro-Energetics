package com.george_vi.electroenergetics.foundation;

import net.minecraft.world.phys.Vec3;

public class WirePoints {
    private double[] values;
    private int size;

    private double minX = Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double minZ = Double.MAX_VALUE;
    private double maxX = Double.MIN_VALUE;
    private double maxY = Double.MIN_VALUE;
    private double maxZ = Double.MIN_VALUE;

    public WirePoints() {
        values = new double[32 * 3];
    }

    public WirePoints(int size) {
        values = new double[size * 3];
    }

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

        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        minZ = Math.min(minZ, z);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        maxZ = Math.max(maxZ, z);
    }

    public void set(int index, double x, double y, double z) {
        if (index >= size || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        int i = index * 3;
        values[i] = x;
        values[i + 1] = y;
        values[i + 2] = z;
        size++;

        minX = minY = minZ = Double.MAX_VALUE;
        maxX = maxY = maxZ = Double.MIN_VALUE;

        for (int j = 0; j < size(); j++) {
            minX = Math.min(minX, values[(j * 3)]);
            minY = Math.min(minY, values[(j * 3) + 1]);
            minZ = Math.min(minZ, values[(j * 3) + 2]);
            maxX = Math.max(maxX, values[(j * 3)]);
            maxY = Math.max(maxY, values[(j * 3) + 1]);
            maxZ = Math.max(maxZ, values[(j * 3) + 2]);
        }
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

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public int size() {
        return size;
    }
}
