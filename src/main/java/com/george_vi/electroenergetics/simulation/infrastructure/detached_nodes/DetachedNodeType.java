package com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes;

import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DetachedNodeType implements StringRepresentable {
    FIXED,
    PHYSICS,
    PHYSICS_UNINITIALIZED;

    public static DetachedNodeType byName(String id) {
        for (DetachedNodeType type : values())
            if (type.getSerializedName().equals(id))
                return type;
        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return Lang.asId(name());
    }

    public boolean isPhysics() {
        return this != FIXED;
    }
}
