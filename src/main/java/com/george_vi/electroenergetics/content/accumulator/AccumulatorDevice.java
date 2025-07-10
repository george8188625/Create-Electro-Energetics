package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class AccumulatorDevice extends SimulatedDevice {
    public AccumulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double storedEnergy = extraData.getDouble("storedEnergy");
        boolean discharging = extraData.getBoolean("discharging");
        if (!discharging)
            bridges.builder(pos).resistor(0, 1, 30);
        else
            bridges.builder(pos)
                    .energyLimitedSource(1, 0, storedEnergy, storedEnergy / -10_000);
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 3 && voltages.size() != 2)
            return;

        double storedEnergy = extraData.getDouble("storedEnergy");
        boolean discharging = extraData.getBoolean("discharging");

        double voltage = storedEnergy / 10_000;

        double vd = voltages.get(new Node(1, pos)) - voltages.get(new Node(0, pos));

        double current = sourceAmps.getOrDefault(new NodeConnection(new Node(1, pos), new Node(1000, pos)), discharging ? 0 : vd / 30);

        if (discharging)
            storedEnergy -= current * vd;
        else
            storedEnergy += current * vd;

        discharging = vd - voltage <= 1;

        extraData.putBoolean("discharging", discharging);
        extraData.putDouble("storedEnergy", storedEnergy);
        return;
    }
}
