package com.george_vi.electroenergetics.foundation.device;

import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDevice;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class SimpleNonTickingElectricalDevice extends SimulatedDevice implements ElectricalDevice {

    public SimpleNonTickingElectricalDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void onDestroy() {
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        sd.removeNodes(pos);
        super.onDestroy();
    }
}
