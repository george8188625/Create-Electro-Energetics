package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.foundation.device.SimpleNonTickingElectricalDevice;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ConnectorDevice extends SimpleNonTickingElectricalDevice {
    public boolean isHVSwitchTarget;

    public ConnectorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void read(CompoundTag tag) {
        isHVSwitchTarget = tag.getBoolean("HVSwitchTarget");
    }

    @Override
    public void write(CompoundTag tag) {
        if (isHVSwitchTarget)
            tag.putBoolean("HVSwitchTarget", true);
    }
}
