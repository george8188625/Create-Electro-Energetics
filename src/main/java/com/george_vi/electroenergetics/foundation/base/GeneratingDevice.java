package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * This device creates a voltage source, and behaves in a way, that doesn't allow the load to take more power than it has available
 */
public abstract class GeneratingDevice<T extends GeneratingDevice.DataHolder> extends SimulatedDevice<T> {

    public GeneratingDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, T extraData) {
        double voltage = getVoltage(pos, level, extraData);

        bridges.builder(pos).energyLimitedSource(0, 1, extraData.storedEnergy, Math.abs(voltage));
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, T extraData) {
        // The stored energy is stored in its internal storage, to a max of 10x, this makes the voltage not drop super low when loaded normally.
        // That is because voltage sources are limited by a resistor in series, but that drops the voltage significantly on normal load.

        double v1 = results.getVoltageAt(pos, 0);
        double v2 = results.getVoltageAt(pos, 1);
        double current = results.getCurrentThrough(pos, 0, 1);

        double power = getPower(pos, level, extraData);

        extraData.storedEnergy -= Math.abs(current * (v1 - v2));

        extraData.storedEnergy = Math.min(extraData.storedEnergy + power, power * 10);
        if (extraData.storedEnergy < 0)
            extraData.storedEnergy = 0;

    }

    protected abstract double getVoltage(BlockPos pos, Level level, T extraData);

    protected abstract double getPower(BlockPos pos, Level level, T extraData);

    public static class DataHolder {
        public double storedEnergy;
    }
}
