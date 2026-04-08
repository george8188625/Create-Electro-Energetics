package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * This device creates a voltage source, and behaves in a way, that doesn't allow the load to take more power than it has available
 */
public abstract class GeneratingDevice extends SimpleElectricalDevice {
    public double storedEnergy;

    public GeneratingDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double voltage = getVoltage();
        if (storedEnergy == 0)
            bridges.builder(pos).resistor(0, 1, 1e+3d);
        else
            bridges.builder(pos).energyLimitedSource(0, 1, storedEnergy, Math.abs(voltage));
    }

    @Override
    public void postTick(SimulationResults results) {
        // The stored energy is stored in its internal storage, to a max of 10x, this makes the voltage not drop super low when loaded normally.
        // That is because voltage sources are limited by a resistor in series, but that drops the voltage significantly on normal load.

        double vd = results.getVoltageAt(pos, 0, 1);
        double current = results.getCurrentThrough(pos, 0, 1);

        double power = getPower();

        storedEnergy -= Math.abs(current * vd);

        storedEnergy = Math.min(storedEnergy + power, power * 10);
        if (storedEnergy < 0)
            storedEnergy = 0;

    }

    protected abstract double getVoltage();

    protected abstract double getPower();

    public static class DataHolder {
        public double storedEnergy;
    }
}
