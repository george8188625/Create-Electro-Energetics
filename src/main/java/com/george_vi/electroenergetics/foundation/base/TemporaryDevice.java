package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.devices.device.DeviceBlock;
import com.george_vi.electroenergetics.foundation.device.TickingElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.LevelTickAccess;

/**
 * Temporary placeholder for devices. Useful in times, when the device type isn't known, but a device must be added.
 */
public class TemporaryDevice extends SimulatedDevice implements TickingElectricalDevice {

    public TemporaryDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (level.getBlockState(pos).getBlock() instanceof DeviceBlock<?> db) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, (Block) db))
                level.scheduleTick(pos, (Block) db, 1);
        } else {
            deviceSD.removeDevice(pos);
        }
    }

    @Override
    public void postTick(SimulationResults results) {

    }
}
