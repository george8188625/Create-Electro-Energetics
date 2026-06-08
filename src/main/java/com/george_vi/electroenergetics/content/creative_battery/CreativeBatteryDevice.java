package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class CreativeBatteryDevice extends SimpleElectricalDevice {

    public double voltage;
    public double acFrequency;
    public float phaseOffset;

    public CreativeBatteryDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {

        boolean ideal = !CEEConfigs.server().simulationConfig.creativeBatteryThevenin.get();
        double voltage = this.voltage;

        if (ideal) {
            if (this.acFrequency == 0)
                bridges.builder(pos)
                        .idealVoltageSource(0, 1, voltage);
            else
                bridges.builder(pos)
                        .connect(0, 1, new ACSource(voltage, this.phaseOffset, 0));
        } else {
            if (this.acFrequency == 0)
                bridges.builder(pos)
                        .voltageSourceWithResistance(0, 1, 0.001d, voltage);
            else
                bridges.builder(pos)
                        .connect(0, 1, new ACSource(voltage, this.phaseOffset, 0.001d));
        }
        bridges.defaultZeroPotential(new InWorldNode(0, pos), 200);
    }

    @Override
    public void read(CompoundTag tag) {
        voltage = tag.contains("Voltage") ? tag.getDouble("Voltage") : 300;
        acFrequency = tag.getDouble("ACFrequency");
        phaseOffset = tag.getFloat("PhaseOffset");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Voltage", voltage);
        tag.putFloat("PhaseOffset", phaseOffset);
        if (acFrequency != 0)
            tag.putDouble("ACFrequency", acFrequency);
    }

    public static class ACSource extends MicroTickingElectricalProperties {
        double voltage;
        double seriesResistance;
        float phaseOffset;

        public ACSource(double voltage, float phaseOffset, double seriesResistance) {
            this.voltage = voltage;
            this.seriesResistance = seriesResistance;
            this.resistance = seriesResistance == 0 ? 1e+11d : seriesResistance;
            this.phaseOffset = phaseOffset;
        }

        @Override
        public void tick(double[] allVoltages, int microTick, int totalMicroTicks, int n1, int n2) {
            if (seriesResistance == 0)
                this.voltageSource = Math.cos(((double) microTick / (totalMicroTicks)) * Mth.TWO_PI + phaseOffset * Mth.DEG_TO_RAD) * voltage;
            else
                this.currentSource = Math.cos(((double) microTick / (totalMicroTicks)) * Mth.TWO_PI + phaseOffset * Mth.DEG_TO_RAD) * voltage / seriesResistance;
        }
    }
}
