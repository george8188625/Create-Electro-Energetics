package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchBlockEntity;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class AccumulatorDevice extends SimulatedDevice {
    public AccumulatorDevice(ResourceLocation id) {
        super(id);
    }

    double capacitance = 100;
    double timeStep = 0.05;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double lastVoltage = extraData.getDouble("LastVoltage");

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * lastVoltage;

        bridges.builder(pos)
                .node(2)
                .resistor(2, 1, 7);
        bridges.bridge(new Node(0, pos), new Node(2, pos), 1 / conductance, 0, -historyCurrent);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        extraData.putDouble("LastVoltage", results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 2));
        double v = Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 2));
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof AccumulatorBlockEntity be) {
                be.energy = (float) ((capacitance / 2) * (v*v)) / 3600;
            }
        }
    }
}
