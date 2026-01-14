package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class CreativeResistorDevice extends SimulatedDevice<CreativeResistorDevice.DataHolder> {
    public CreativeResistorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .connect(0, 1, extraData.properties);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.properties = ElectricalProperties.resistor(Mth.clamp(tag.getDouble("Resistance"), 0.01, 1_000_000));
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Resistance", extraData.properties.resistance);
        return tag;
    }

    public static class DataHolder {
        public ElectricalProperties properties;
    }
}
