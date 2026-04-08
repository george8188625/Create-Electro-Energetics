package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class TriPolarEnergyMeterDevice extends SimpleElectricalDevice {
    public double totalEnergy;
    public boolean isClosed;
    public EnergyMeterBlockEntity be;

    public TriPolarEnergyMeterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.isClosed) {
            bridges.builder(pos).resistor(0, 3, 0.001);
            bridges.builder(pos).resistor(2, 5, 0.001);
            bridges.builder(pos).resistor(1, 4, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
            bridges.builder(pos).resistor(1, 2, 9999);
        }
    }

    @Override
    public void postTick(SimulationResults results) {

        double vd1 = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 3);
        double vd2 = results.getVoltageAt(pos, 2) - results.getVoltageAt(pos, 5);
        double v1 = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double v2 = results.getVoltageAt(pos, 1) - results.getVoltageAt(pos, 2);

        double amps1 = vd1 / 0.001;
        double amps2 = vd2 / 0.001;

        if (Math.abs(amps1) > 0.01 && this.isClosed)
            this.totalEnergy += (amps1 * v1 / 72000) / 1000;

        if (Math.abs(amps2) > 0.01 && this.isClosed)
            this.totalEnergy -= (amps2 * v2 / 72000) / 1000;

        if (!level.isLoaded(pos))
            return;

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.setTotalEnergy((float) this.totalEnergy);
                this.be.activePower = this.isClosed ? amps1 * v1 - amps2 * v2 : 0;
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.totalEnergy = tag.getDouble("TotalEnergy");
        this.isClosed = tag.getBoolean("Closed");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("TotalEnergy", this.totalEnergy);
        tag.putBoolean("Closed", this.isClosed);
    }
}
