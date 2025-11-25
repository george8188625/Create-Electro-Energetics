package com.george_vi.electroenergetics.content.electronic_components.diode;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class DiodeDevice extends SimulatedDevice<DiodeDevice.DataHolder> {
    public DiodeDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        // Thanks, ChatGPT

        double iS = 10e-12d;
        double vT = 0.046d;

        extraData.voltage = Mth.clamp(extraData.voltage, -0.8, 0.8);

        double Cj = 1e-12d;
        double gCap = Cj / bridges.getTimeStep();
        double iEqCap = gCap * extraData.voltage;

        double g = Math.max(1e-12d, (iS / vT) * Math.exp(extraData.voltage / vT)) + gCap;

        double resistance = 1 / g;
        double currentSource = iS * (Math.exp(extraData.voltage / vT) - 1) - g * extraData.voltage;

        bridges.builder(pos)
                .resistor(0, 1, resistance)
                .idealCurrentSource(0, 1, -currentSource - iEqCap);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        extraData.voltage = results.getVoltageAt(pos, 1, 0);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.voltage = tag.getDouble("Voltage");
        dataHolder.temp = tag.getFloat("Temp");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Voltage", extraData.voltage);
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public double voltage;
        public float temp;
    }
}

