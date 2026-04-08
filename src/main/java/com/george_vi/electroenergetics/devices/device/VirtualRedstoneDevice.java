package com.george_vi.electroenergetics.devices.device;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public interface VirtualRedstoneDevice {
    default void updateRedstoneInput(int power, Direction direction) {

    }

    default int getRedstonePower(Direction direction) {
        return 0;
    }

    default void updateRedstoneOutput(@Nullable Direction direction) {

    }
}
