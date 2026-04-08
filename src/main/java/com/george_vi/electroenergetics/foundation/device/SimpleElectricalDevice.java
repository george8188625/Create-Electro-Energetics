package com.george_vi.electroenergetics.foundation.device;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class SimpleElectricalDevice extends SimulatedDevice implements TickingElectricalDevice {

    public SimpleElectricalDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }


    @Override
    public void preTick(BridgeCollector bridges) {

    }

    @Override
    public void postTick(SimulationResults results) {

    }

    @Override
    public void onDestroy() {
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        sd.removeNodes(pos);
        super.onDestroy();
    }
}
