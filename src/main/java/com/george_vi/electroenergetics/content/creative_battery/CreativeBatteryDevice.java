package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CreativeBatteryDevice extends SimulatedDevice<CreativeBatteryDevice.DataHolder> {
    public CreativeBatteryDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double voltage = extraData.voltage;

        // AC experiment
        // voltage = Math.sin(AnimationTickHolder.getTicks() / 5d) * voltage;

        bridges.builder(pos)
                .idealVoltageSource(0, 1, voltage);
        bridges.defaultZeroPotential(new InWorldNode(0, pos), 200);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.voltage = tag.contains("Voltage") ? tag.getDouble("Voltage") : 12;
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Voltage", extraData.voltage);
        return tag;
    }

    public static class DataHolder {
        public double voltage;
    }
}
