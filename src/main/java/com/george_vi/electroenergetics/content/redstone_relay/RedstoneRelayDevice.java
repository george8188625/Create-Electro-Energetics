package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRelayDevice extends SimulatedDevice<RedstoneRelayDevice.DataHolder> {
    public RedstoneRelayDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (CEEBlocks.REDSTONE_RELAY.has(state))
                extraData.powered = state.getValue(RedstoneRelayBlock.POWERED);
        }

        if (extraData.powered ^ extraData.inverted)
            bridges.builder(pos)
                    .resistor(0, 1, 0.1);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.inverted = tag.getBoolean("Inverted");
        dataHolder.powered = tag.getBoolean("Powered");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        if (extraData.powered)
            tag.putBoolean("Powered", true);
        if (extraData.inverted)
            tag.putBoolean("Inverted", true);
        return tag;
    }

    public static class DataHolder {
        public boolean inverted;
        public boolean powered;
    }
}
