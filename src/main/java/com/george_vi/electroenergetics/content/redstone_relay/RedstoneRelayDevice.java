package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRelayDevice extends SimulatedDevice {
    public RedstoneRelayDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (CEEBlocks.REDSTONE_RELAY.has(state))
                extraData.putBoolean("Powered", state.getValue(RedstoneRelayBlock.POWERED));
        }

        if (extraData.getBoolean("Powered") ^ extraData.getBoolean("Inverted"))
            bridges.builder(pos)
                    .resistor(0, 1, 0.1);
    }
}
