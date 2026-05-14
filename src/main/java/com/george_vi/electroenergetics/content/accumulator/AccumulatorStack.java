package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;

public enum AccumulatorStack implements StringRepresentable {
    SINGLE, // A single cell in the center
    DOUBLE_PARALLEL, // Both cells in the same direction
    DOUBLE_OPPOSITE; // Both cells in the opposite direction

    public boolean isDouble() {
        return this != SINGLE;
    }

    public boolean isSingle() {
        return this == SINGLE;
    }

    public NodeConfigurator getNodes() {
        if (this == SINGLE)
            return CEENodeConfigurations.ACCUMULATOR_SINGLE;
        if (this == DOUBLE_OPPOSITE)
            return CEENodeConfigurations.ACCUMULATOR_OPPOSITE;
        else
            return CEENodeConfigurations.ACCUMULATOR_PARALLEL;
    }

    @Override
    public String getSerializedName() {
        return Lang.asId(name());
    }
}
