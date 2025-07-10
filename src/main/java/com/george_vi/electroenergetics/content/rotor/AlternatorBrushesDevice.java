package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class AlternatorBrushesDevice extends SimulatedDevice {
    public AlternatorBrushesDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (!level.isLoaded(pos))
            return;
        double stress = extraData.getFloat("stress");
        double voltage = extraData.getFloat("voltage");

        bridges.builder(pos).energyLimitedSource(0, 1, stress, Math.abs(voltage));
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {

    }
}
