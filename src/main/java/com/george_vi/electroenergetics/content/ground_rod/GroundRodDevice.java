package com.george_vi.electroenergetics.content.ground_rod;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class GroundRodDevice extends SimulatedDevice<Void> {
    public GroundRodDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, Void extraData) {
        bridges.builder(pos)
                .ground(0, 1);
    }

    @Override
    public Void read(CompoundTag tag) {
        return null;
    }

    @Override
    public CompoundTag write(Void extraData) {
        return new CompoundTag();
    }
}
