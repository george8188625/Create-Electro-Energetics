package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.foundation.device.SimpleNonTickingElectricalDevice;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.george_vi.simulateddevices.device.VirtualRedstoneDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class InsulatorDevice extends SimpleNonTickingElectricalDevice implements VirtualRedstoneDevice {
    public boolean powered;
    public byte[] power = new byte[Direction.values().length];
    
    public InsulatorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void updateRedstoneInput(int power, Direction direction) {
        byte[] powerLevels = this.power;
        powerLevels[direction.ordinal()] = (byte) power;
        boolean p = false;
        for (byte b : powerLevels) {
            if (b > 0) {
                p = true;
                break;
            }
        }
        this.powered = p;
    }

    @Override
    public void read(CompoundTag tag) {
        this.powered = tag.getBoolean("Closed");
        byte[] arr = tag.getByteArray("Powered");
        if (arr.length == this.power.length)
            this.power = arr;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putByteArray("Power", this.power);
        if (this.powered)
            tag.putBoolean("Powered", true);
    }

}
