package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.foundation.device.SimpleNonTickingElectricalDevice;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DoubleConnectorDevice extends SimpleNonTickingElectricalDevice {

    public DoubleConnectorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }
}
