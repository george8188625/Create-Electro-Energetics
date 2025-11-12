package com.george_vi.electroenergetics.simulation.util;

public class DataPacker {
    public static long pack(int id1, int id2) {
        return ((long) id1 << 32) | (id2 & 0xFFFFFFFFL);
    }

    public static int unpackFirstI(long packed) {
        return (int) ((packed >> 32) & 0xFFFFFFFFL);
    }

    public static int unpackSecondI(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }
}
