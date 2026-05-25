package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class GaugeDevice extends SimpleElectricalDevice {
    public final boolean voltmeter;
    public ElectricGaugeBlockEntity be;
    public RMSHolder rmsVoltages;
    public float temp;

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

        float factor = voltmeter ? 1f : 30f;

        float loss = (float) ((vd * vd) / (voltmeter ? 1_000_000 : 0.01)) / factor;

        this.temp = ElectricalDevice.updateTemp(this.temp, loss);
        double maxVoltmeterVoltage = CEEConfigs.server().voltageValues.maxVoltmeterVoltage.get();
        double maxAmmeterCurrent = CEEConfigs.server().voltageValues.maxAmmeterCurrent.get();

        float finalTemp = ElectricalDevice.finalTempAt((float) (voltmeter ?
                        maxVoltmeterVoltage * maxVoltmeterVoltage / 1_000_000 :
                        maxAmmeterCurrent * maxAmmeterCurrent * 0.01d)) * factor;
        ElectricalDevice.handleTemp(level, pos, deviceSD, temp, finalTemp * 0.9f, finalTemp);
    }

    @Override
    public void read(CompoundTag tag) {
        this.rmsVoltages = new RMSHolder(2);
        this.rmsVoltages.read(tag, "Voltages");
        this.temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        this.rmsVoltages.write(tag, "Voltages");
        tag.putFloat("Temp", this.temp);
    }
}
