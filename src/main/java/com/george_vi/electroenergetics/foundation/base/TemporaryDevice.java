package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDevice;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.george_vi.simulateddevices.device.TickingDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.LevelTickAccess;

/**
 * Temporary placeholder for devices. Useful in times, when the device type isn't known, but a device must be added.
 * The first tick, it checks if the block in this position is a {@link DeviceBlock}, and creates a block tick.
 * (All {@link DeviceBlock} implementations should add / replace the device on their block ticks.)
 * Otherwise, the device is removed, as it was probably misplaced.
 */
public class TemporaryDevice extends SimulatedDevice implements TickingDevice {

    public TemporaryDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void tick() {

        if (level.getBlockState(pos).getBlock() instanceof DeviceBlock db) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, (Block) db))
                level.scheduleTick(pos, (Block) db, 1);
        } else {
            deviceSD.removeDevice(pos);
        }
    }
}
