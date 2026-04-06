package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Provides a simple-to-use interface to use basic redstone on unloaded chunks.
 * @param <T> the data holder class type
 */
public interface VirtualRedstoneDevice<T> {
    void updateRedstoneInput(Level level, BlockPos pos, Direction direction, T extraData, int power);

    default int getRedstonePower(Level level, BlockPos pos, Direction direction, T extraData) {
        return 0;
    }

    default void updateRedstoneOutput(Level level, BlockPos pos, @Nullable Direction direction) {

    }
}
