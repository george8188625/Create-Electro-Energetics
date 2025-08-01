package com.george_vi.electroenergetics.simulation;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.DoubleSupplier;

public class WireType {

    final DoubleSupplier resistance;
    final PartialModel model;

    public WireType(DoubleSupplier resistance, PartialModel model) {
        this.resistance = resistance;
        this.model = model;
    }

    public double getResistance() {
        return resistance.getAsDouble();
    }

    public PartialModel getModel() {
        return model;
    }
}
