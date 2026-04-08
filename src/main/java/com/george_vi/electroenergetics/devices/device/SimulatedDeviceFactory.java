package com.george_vi.electroenergetics.devices.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface SimulatedDeviceFactory<T extends SimulatedDevice> {
    T create(SimulatedDeviceType<T> type, Level level, BlockPos pos, DevicesSavedData sd);
}
