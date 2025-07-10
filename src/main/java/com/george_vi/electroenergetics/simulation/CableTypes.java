package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CableTypes {
    static Map<ResourceLocation, CableType> BY_ID = new HashMap<>();

    // Not yet implemented. Cables will be blocks that you can place in the world, they can have one or more conductors

    public static final CableType DUAL = register(new CableType(CreateElecrtoEnergetics.rl("dual"), 2, 0.01));

    public static CableType register(CableType type) {
        BY_ID.put(type.id, type);
        return type;
    }

    public static CableType get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static class CableType {
        final ResourceLocation id;
        final int conductors;
        final double resistance;

        public CableType(ResourceLocation id, int conductors, double resistance) {
            this.id = id;
            this.conductors = conductors;
            this.resistance = resistance;
        }

        public ResourceLocation getID() {
            return id;
        }
        public int getConductors() {
            return conductors;
        }
        public double getResistance() {
            return resistance;
        }
    }
}
