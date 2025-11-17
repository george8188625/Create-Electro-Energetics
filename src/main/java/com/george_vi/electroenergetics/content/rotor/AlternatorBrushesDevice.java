package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.foundation.base.GeneratingDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AlternatorBrushesDevice extends GeneratingDevice {
    public AlternatorBrushesDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos))
            super.preTick(pos, level, bridges, extraData);
    }

    @Override
    protected double getVoltage(BlockPos pos, Level level, CompoundTag extraData) {
        return extraData.getFloat("Voltage");
    }

    @Override
    protected double getPower(BlockPos pos, Level level, CompoundTag extraData) {
        return extraData.getFloat("Stress");
    }
}
