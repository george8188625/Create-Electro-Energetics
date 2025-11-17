package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * This device creates a voltage source, and behaves in a way, that doesn't allow the load to take more power than it has available
 */
public abstract class GeneratingDevice extends SimulatedDevice {

    public GeneratingDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double voltage = getVoltage(pos, level, extraData);

        bridges.builder(pos).energyLimitedSource(0, 1, extraData.getDouble("StoredEnergy"), Math.abs(voltage));
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        // The stored energy is stored in its internal storage, to a max of 10x, this makes the voltage not drop super low when loaded normally.
        // That is because voltage sources are limited by a resistor in series, but that drops the voltage significantly on normal load.

        double v1 = results.getVoltageAt(pos, 0);
        double v2 = results.getVoltageAt(pos, 1);
        double current = results.getCurrentThrough(pos, 0, 1);

        double power = getPower(pos, level, extraData);

        double storedEnergy = extraData.getDouble("StoredEnergy");
        storedEnergy -= Math.abs(current * (v1 - v2));

        storedEnergy = Math.min(storedEnergy + power, power * 10);
        if (storedEnergy < 0)
            storedEnergy = 0;

        extraData.putDouble("StoredEnergy", storedEnergy);
    }

    protected abstract double getVoltage(BlockPos pos, Level level, CompoundTag extraData);

    protected abstract double getPower(BlockPos pos, Level level, CompoundTag extraData);
}
