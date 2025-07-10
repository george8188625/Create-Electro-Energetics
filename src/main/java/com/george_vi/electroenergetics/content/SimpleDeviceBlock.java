package com.george_vi.electroenergetics.content;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.simulation.*;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleDeviceBlock extends Block implements DeviceBlock {
    public SimpleDeviceBlock(Properties properties) {
        super(properties);
    }

    protected abstract SimulatedDevice getDevice();

    protected CompoundTag getExtraData(Level level, BlockState state, BlockPos pos) {return new CompoundTag();}

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Integer> nodes = new ArrayList<>();
        for (int i : getNodePositions(level, pos, state).values())
            nodes.add(i);
        InfrastructureSavedData.load(level).addDevice(pos, getDevice(), getExtraData(level, state, pos), nodes);
        super.tick(state, level, pos, random);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && state.getBlock() != level.getBlockState(pos).getBlock() && shouldReplaceDeviceFor(state, newState)) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            for (Node node : sd.getNodesAt(pos))
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.INSULATED_WIRE.asStack(sd.getConnections(node).size() * 8));
            sd.removeDevice(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected boolean shouldReplaceDeviceFor(BlockState thisState, BlockState newState) {
        return true;
    }
}
