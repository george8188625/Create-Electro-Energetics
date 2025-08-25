package com.george_vi.electroenergetics.content.electronic_components;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ResistorDevice extends SimulatedDevice {
    public ResistorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos)
                .resistor(0, 1, extraData.getDouble("Resistance") + 0.0001);
    }
}
