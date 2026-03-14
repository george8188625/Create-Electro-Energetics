package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.simulator.MicroTickingElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class CreativeBatteryDevice extends SimulatedDevice<CreativeBatteryDevice.DataHolder> {
    public CreativeBatteryDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {

        boolean ideal = !CEEConfigs.server().simulationConfig.creativeBatteryThevenin.get();
        double voltage = extraData.voltage;

        if (ideal) {
            if (extraData.acFrequency == 0)
                bridges.builder(pos)
                        .idealVoltageSource(0, 1, voltage);
            else
                bridges.builder(pos)
                        .connect(0, 1, new ACSource(voltage, 0));
        } else {
            if (extraData.acFrequency == 0)
                bridges.builder(pos)
                        .voltageSourceWithResistance(0, 1, 0.001d, voltage);
            else
                bridges.builder(pos)
                        .connect(0, 1, new ACSource(voltage, 0.001d));
        }
        bridges.defaultZeroPotential(new InWorldNode(0, pos), 200);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.voltage = tag.contains("Voltage") ? tag.getDouble("Voltage") : 300;
        dataHolder.acFrequency = tag.getDouble("ACFrequency");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Voltage", extraData.voltage);
        if (extraData.acFrequency != 0)
            tag.putDouble("ACFrequency", extraData.acFrequency);
        return tag;
    }

    public static class DataHolder {
        public double voltage;
        public double acFrequency;
    }

    public static class ACSource extends MicroTickingElectricalProperties {
        double voltage;
        double seriesResistance;

        public ACSource(double voltage, double seriesResistance) {
            this.voltage = voltage;
            this.seriesResistance = seriesResistance;
            this.resistance = seriesResistance == 0 ? 1e+11d : seriesResistance;

        }

        @Override
        public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
            if (seriesResistance == 0)
                this.voltageSource = Math.cos(((double) microTick / (totalMicroTicks)) * Mth.TWO_PI) * voltage;
            else
                this.currentSource = Math.cos(((double) microTick / (totalMicroTicks)) * Mth.TWO_PI) * voltage / seriesResistance;
        }
    }
}
