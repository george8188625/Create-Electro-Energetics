package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WireTypes {
    static Map<ResourceLocation, WireType> BY_ID = new HashMap<>();

    // Multiple wire types are not yet implemented. Wires are in-air conductors that are placed on connectors.

    public static final WireType STANDARD = register(new WireType(CreateElecrtoEnergetics.rl("standard"), 0.01, CEEPartialModels.WIRE_SEGMENT));

    public static WireType register(WireType type) {
        BY_ID.put(type.id, type);
        return type;
    }

    public static WireType get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static class WireType {
        final ResourceLocation id;
        final double resistance;
        final PartialModel model;

        public WireType(ResourceLocation id, double resistance, PartialModel model) {
            this.id = id;
            this.resistance = resistance;
            this.model = model;
        }

        public ResourceLocation getID() {
            return id;
        }
        public double getResistance() {
            return resistance;
        }
        public PartialModel getModel() {
            return model;
        }
    }
}
