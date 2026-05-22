package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BundledWireTerminationDevice extends SimpleElectricalDevice {
    public BundledWireTerminationDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }
}
