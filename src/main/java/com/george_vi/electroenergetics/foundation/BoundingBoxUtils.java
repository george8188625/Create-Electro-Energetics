package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.Vec3i;

public class BoundingBoxUtils {
    public static boolean isIn(Vec3i pos, Vec3i origin, Vec3i size) {
        return pos.getX() >= origin.getX() && pos.getX() < origin.getX() + size.getX() &&
                pos.getY() >= origin.getY() && pos.getY() < origin.getY() + size.getY() &&
                pos.getZ() >= origin.getZ() && pos.getZ() < origin.getZ() + size.getZ();
    }
}
