package com.george_vi.electroenergetics.devices.device;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This is a type class for devices.
 * Devices are data structures for blocks with behavior that executes, even tho they are unloaded.
 */
public abstract class SimulatedDevice {
    public final Level level;
    public final BlockPos pos;
    public final DevicesSavedData deviceSD;
    public final SimulatedDeviceType<?> type;
    private boolean isValid = true;

    protected SimulatedDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        this.level = level;
        this.pos = pos;
        this.deviceSD = deviceSD;
        this.type = type;
    }

    /**
     * Write this device's data.
     */
    public void write(CompoundTag tag) {

    }

    /**
     * Read data for this device.
     */
    public void read(CompoundTag tag) {

    }

    /**
     * A general update for this device.
     */
    public void update() {

    }

    /**
     *
     * @return Whether the device should be removed when the block has been changed from/to the specified state
     */
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return true;
    }

    /**
     * Called when the device is removed
     */
    public void onDestroy() {

    }

    public boolean isValid() {
        return isValid;
    }

    void invalidate() {
        isValid = false;
    }
}
