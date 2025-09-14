package com.george_vi.electroenergetics.content.electronic_components;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class DiodeDevice extends SimulatedDevice {
    public DiodeDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos)
                .resistor(0, 1, extraData.getBoolean("Forward") ? 999999 : 0.5);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        boolean oldBias = extraData.getBoolean("Forward");
        boolean bias = results.getVoltageAt(pos, 0) > results.getVoltageAt(pos, 1);
        if (!oldBias && Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1)) < 0.001)
            bias = false;
        extraData.putBoolean("Forward", bias);
    }
}
