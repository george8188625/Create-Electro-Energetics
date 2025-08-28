package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
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

    static final int MAX_ENERGY = 100_000;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (extraData.getBoolean("Source"))
            bridges.builder(pos)
                    .energyLimitedSource(0, 1, extraData.getDouble("StoredEnergy"), extraData.getDouble("Voltage"));
        else
            bridges.builder(pos)
                    .resistor(0, 1, extraData.contains("Resistance") ? extraData.getDouble("Resistance") : 999999);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double storedEnergy = extraData.getDouble("StoredEnergy");
        double conversionRate = 34;

        double vd = Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1));
        if (extraData.getBoolean("Source")) {

            double current = results.getCurrentThrough(pos, 0, 1);
            double power = Math.abs(current) * vd;

            storedEnergy -= power;

            if (storedEnergy < MAX_ENERGY && level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
                if (energyStorage != null)
                    storedEnergy += energyStorage.extractEnergy((int) ((MAX_ENERGY - storedEnergy) / conversionRate), false) * conversionRate;
            }

            extraData.putDouble("StoredEnergy", storedEnergy);
            return;
        }

        double power = vd * results.getCurrentThrough(pos, 0, 1);

        storedEnergy += power;

        if (storedEnergy > 0 && level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
            if (energyStorage != null)
                storedEnergy -= energyStorage.receiveEnergy((int) (storedEnergy / conversionRate), false) * conversionRate;
        }

        extraData.putDouble("StoredEnergy", storedEnergy);

        if (vd > 1)
            extraData.putDouble("Resistance", Math.max(5, vd / (Math.max((MAX_ENERGY - storedEnergy), 0.01) / vd)));
    }
}
