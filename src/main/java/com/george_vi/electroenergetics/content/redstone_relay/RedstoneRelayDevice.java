package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRelayDevice extends SimpleElectricalDevice {
    public boolean inverted;
    public boolean powered;

    public RedstoneRelayDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (CEEBlocks.REDSTONE_RELAY.has(state)) {
                this.powered = state.getValue(RedstoneRelayBlock.POWERED);
                this.inverted = state.getValue(RedstoneRelayBlock.INVERTED);
            }
        }

        if (this.powered ^ this.inverted)
            bridges.builder(pos)
                    .resistor(0, 1, 0.1);
    }

    @Override
    public void read(CompoundTag tag) {
        inverted = tag.getBoolean("Inverted");
        powered = tag.getBoolean("Powered");
    }

    @Override
    public void write(CompoundTag tag) {
        if (this.powered)
            tag.putBoolean("Powered", true);
        if (this.inverted)
            tag.putBoolean("Inverted", true);
    }
}
