package com.george_vi.electroenergetics.content.frequency_meter;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class FrequencyMeterDevice extends SimpleElectricalDevice {

    public FrequencyMeterBlockEntity be;

    public double prevPeriod = 0;
    public double prevCross = 0;
    public double prevV = 0;
    public int ticks = 0;

    public FrequencyMeterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 1_000_000);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof FrequencyMeterBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                float v = calculateFrequency(pos, results) % 360;
                if ((ticks % 10 == 0 && Math.abs(be.frequency - v) > 0.01)) {
                    be.frequency = v;
                    be.sendData();
                }
            }
        }
    }

    double[] p;
    double[] s;
    private float calculateFrequency(BlockPos pos, SimulationResults results) {
        p = results.getVoltages(new InWorldNode(0, pos), p);
        s = results.getVoltages(new InWorldNode(1, pos), s);

        for (int i = 0; i < p.length; i++) {
            this.ticks++;
            double v = p[i] - s[i];
            if (Math.abs(v) < 1e-6d)
                v = 0;

            if (v > 0 && this.prevV <= 0) {
                double interpolated = this.ticks + (-this.prevV / (v - this.prevV));
                this.prevPeriod = interpolated - this.prevCross;
                this.prevCross = interpolated;
            }

            this.prevV = v;
        }

        double actualPeriod = Math.max(prevPeriod, ticks - prevCross);

        double frequency = Math.abs(actualPeriod) < 1e-3d ? 0 : 1 / actualPeriod;
        frequency *= (1 << CEEConfigs.server().simulationConfig.microTickBits.get()) * 20;

        return (float) frequency;
    }

    @Override
    public void read(CompoundTag tag) {
        prevPeriod = tag.getDouble("PrevPeriod");
        prevCross = tag.getDouble("PrevCross");
        prevV = tag.getDouble("PrevVoltage");
        ticks = tag.getInt("Ticks");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("PrevPeriod", prevPeriod);
        tag.putDouble("PrevCross", prevCross);
        tag.putDouble("PrevVoltage", prevV);
        tag.putInt("Ticks", ticks);
    }
}
