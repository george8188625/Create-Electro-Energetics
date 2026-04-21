package com.george_vi.electroenergetics.foundation;

import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;

/**
 * Calculates the Root Mean Square (RMS) of values.
 * Only the latest n values are used, where n is windowSize.
 */
public class RMSHolder {
    final int windowSize;
    int pointer = 0;
    double rms;
    final double[] values;

    public RMSHolder(int windowSize) {
        this.windowSize = windowSize;
        values = new double[windowSize];
    }

    public void add(double value) {
        values[pointer] = value;
        pointer = (pointer + 1) % windowSize;
        double sum = 0;
        for (int i = 0; i < windowSize; i++)
            sum += values[i] * values[i];
        sum /= windowSize;
        rms = Math.sqrt(sum);
    }

    public double get() {
        return rms;
    }

    public void read(CompoundTag tag, String key) {
        long[] arr = tag.getLongArray(key);

        for (int i = 0; i < Math.min(arr.length, windowSize); i++)
            values[i] = Double.longBitsToDouble(arr[i]);

        pointer = tag.getInt(key + "Pointer") % windowSize;

        double sum = 0;
        for (int i = 0; i < windowSize; i++)
            sum += values[i] * values[i];
        sum /= windowSize;
        rms = Math.sqrt(sum);
    }

    public void write(CompoundTag tag, String key) {
        tag.putLongArray(key, Arrays.stream(values).mapToLong(Double::doubleToRawLongBits).toArray());
        tag.putInt(key + "Pointer", pointer);
    }

    public double getSigned() {
        double max = 0, min = 0, sum = 0;
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            sum += v * v;

            if (i == 0) {
                max = min = v;
                continue;
            }

            if (v > max) max = v;
            if (v < min) min = v;
        }

        double rms = Math.sqrt(sum / values.length);

        if (min < 0 && max < 0)
            rms = -rms;
        else if (min < 0 && max < min * -0.1)
            rms = -rms;
        return rms;
    }
}
