package com.george_vi.electroenergetics.devices.device;

public class DeviceFeatureType {
    static int globalID;
    private static boolean frozen = false;
    public final Class<?> featureClazz;
    public final int id;

    public DeviceFeatureType(Class<?> featureClazz) {
        if (frozen)
            throw new IllegalStateException("Cannot create a DeviceFeatureType after it has been frozen");
        this.featureClazz = featureClazz;
        this.id = globalID++;
    }

    public static void freeze() {
        frozen = true;
    }
}
