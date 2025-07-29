package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class TriPolarEnergyMeterDevice extends SimulatedDevice {
    public TriPolarEnergyMeterDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (extraData.getBoolean("Closed")) {
            bridges.builder(pos).resistor(0, 3, 0.001);
            bridges.builder(pos).resistor(2, 5, 0.001);
            bridges.builder(pos).resistor(1, 4, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
            bridges.builder(pos).resistor(1, 2, 9999);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 6)
            return;
        double vd1 = voltages.get(new Node(0, pos)) - voltages.get(new Node(3, pos));
        double vd2 = voltages.get(new Node(2, pos)) - voltages.get(new Node(5, pos));
        double v1 = Math.abs(voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos)));
        double v2 = Math.abs(voltages.get(new Node(1, pos)) - voltages.get(new Node(2, pos)));

        double amps1 = vd1 / 0.001;
        double amps2 = vd2 / 0.001;
        double totalEnergy = extraData.getDouble("TotalEnergy");

        if (Math.abs(amps1) > 0.01 && extraData.getBoolean("Closed"))
            totalEnergy -= (amps1 * v1 / 72000) / 1000;

        if (Math.abs(amps2) > 0.01 && extraData.getBoolean("Closed"))
            totalEnergy += (amps2 * v2 / 72000) / 1000;

        extraData.putDouble("TotalEnergy", totalEnergy);

        if (!level.isLoaded(pos))
            return;

        if (level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity be)
            be.setTotalEnergy((float) totalEnergy);
    }
}
