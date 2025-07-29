package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
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
        double voltage = extraData.getFloat("Voltage");

        bridges.builder(pos).energyLimitedSource(0, 1, extraData.getDouble("StoredEnergy"), Math.abs(voltage));
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 3)
            return;

        double v1 = voltages.get(new Node(0, pos));
        double v2 = voltages.get(new Node(1, pos));
        double current = 0;
        if (!sourceAmps.isEmpty())
            current = sourceAmps.values().stream().mapToDouble(c -> c).sum();

        double power = extraData.getFloat("Stress");

        double storedEnergy = extraData.getDouble("StoredEnergy");
        storedEnergy -= Math.abs(current * (v1 - v2));

        storedEnergy = Math.min(storedEnergy + power, power * 10);

        extraData.putDouble("StoredEnergy", storedEnergy);
    }
}
