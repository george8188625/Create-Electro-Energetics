package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Map;

public class ConverterDevice extends SimulatedDevice {
    public ConverterDevice(ResourceLocation id) {
        super(id);
    }

    static final int MAX_ENERGY = 10_000;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (extraData.getBoolean("source"))
            bridges.builder(pos)
                    .energyLimitedSource(0, 1, extraData.getDouble("storedEnergy"), extraData.getDouble("voltage"));
        else
            bridges.builder(pos)
                    .resistor(0, 1, extraData.contains("resistance") ? extraData.getDouble("resistance") : 999999);
    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (voltages.size() != 2 && voltages.size() != 3)
            return;
        double storedEnergy = extraData.getDouble("storedEnergy");

        if (extraData.getBoolean("source")) {
            double vd = Math.abs(voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos)));

            double current = sourceAmps.getOrDefault(new NodeConnection(pos, 0, 1000), 0d);
            double power = Math.abs(current) * vd;

            storedEnergy -= power;

            if (storedEnergy < MAX_ENERGY && level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
                if (energyStorage != null)
                    storedEnergy += energyStorage.extractEnergy((int) ((MAX_ENERGY - storedEnergy) / 3.556), false) * 3.556;
            }

            extraData.putDouble("storedEnergy", storedEnergy);
            return;
        }

        double vd = Math.abs(voltages.get(new Node(0, pos)) - voltages.get(new Node(1, pos)));

        double power = (vd * vd) / (extraData.contains("resistance") ? extraData.getDouble("resistance") : 999999);

        storedEnergy += power;

        if (storedEnergy > 0 && level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
            if (energyStorage != null)
                storedEnergy -= energyStorage.receiveEnergy((int) (storedEnergy / 3.556), false) * 3.556;
        }

        extraData.putDouble("storedEnergy", storedEnergy);

        if (vd > 1)
            extraData.putDouble("resistance", Math.max(30, vd / (Math.max((MAX_ENERGY - storedEnergy), 0.01) / vd)));
    }
}
