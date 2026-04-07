package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class GaugeDevice extends SimpleElectricalDevice {
    public final boolean voltmeter;
    public ElectricGaugeBlockEntity be;
    public RMSHolder rmsVoltages;

    public GaugeDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, boolean voltmeter) {
        super(level, pos, deviceSD, type);
        this.voltmeter = voltmeter;
    }


    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos).resistor(0, 1, voltmeter ? 1_000_000 : 0.01);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (!level.isLoaded(pos))
            return;

        double vd = results.getVoltageAt(pos, 0, 1);
        this.rmsVoltages.add(vd);
        vd = this.rmsVoltages.get();
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricGaugeBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.voltage = vd;
                this.be.setValue(voltmeter ? Math.abs(vd) : Math.abs(vd) / 0.01);
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.rmsVoltages = new RMSHolder(2);
        this.rmsVoltages.read(tag, "Voltages");
    }

    @Override
    public void write(CompoundTag tag) {
        this.rmsVoltages.write(tag, "Voltages");
    }
}
