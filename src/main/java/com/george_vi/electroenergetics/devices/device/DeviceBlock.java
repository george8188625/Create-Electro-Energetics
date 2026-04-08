package com.george_vi.electroenergetics.devices.device;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface DeviceBlock<T extends SimulatedDevice> {

    SimulatedDeviceType<T> getDevice();

    default CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        return new CompoundTag();
    }
}
