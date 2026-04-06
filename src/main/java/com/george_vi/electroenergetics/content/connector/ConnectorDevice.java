package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ConnectorDevice extends SimulatedDevice<ConnectorDevice.DataHolder> {
    public ConnectorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.isHVSwitchTarget = tag.getBoolean("HVSwitchTarget");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        if (extraData.isHVSwitchTarget)
            tag.putBoolean("HVSwitchTarget", true);
        return tag;
    }

    @Override
    public boolean ticks() {
        return false;
    }

    public static class DataHolder {
        public boolean isHVSwitchTarget;
    }
}
