package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EnergyMeterDevice extends SimpleElectricalDevice {
    public double totalEnergy;
    public boolean isClosed;
    public EnergyMeterBlockEntity be;
    
    public EnergyMeterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (isClosed) {
            bridges.builder(pos).resistor(0, 2, 0.001);
            bridges.builder(pos).resistor(1, 3, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
        }
    }

    @Override
    public void postTick(SimulationResults results) {
        double[] v0s = results.getVoltages(new InWorldNode(0, pos));
        double[] v1s = results.getVoltages(new InWorldNode(1, pos));
        double[] v2s = results.getVoltages(new InWorldNode(2, pos));
        double power = 0;

        for (int i = 0; i < v0s.length; i++) {

            double amps = (v0s[i] - v2s[i]) / 0.001;

            if (Math.abs(amps) > 0.01 && this.isClosed) {
//                energy += amps * (v0s[i] - v1s[i]) * (0.05/8);
                this.totalEnergy += (amps * (v0s[i] - v1s[i]) / 72000) / (1000 * v0s.length);
                power += amps * (v0s[i] - v1s[i]);
            }
        }

        power /= v0s.length;

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
                this.be.activePower = this.isClosed ? power : 0;
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        totalEnergy = tag.getDouble("TotalEnergy");
        isClosed = tag.getBoolean("Closed");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putBoolean("Closed", isClosed);
    }
}
