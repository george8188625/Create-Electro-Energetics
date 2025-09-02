package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
        List<Integer> nodes = new ArrayList<>(getNodePositions(level, pos, state).keySet());

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        sd.addDevice(pos, getDevice(), getExtraData(level, state, pos), nodes);
        super.tick(state, level, pos, random);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && state.getBlock() != level.getBlockState(pos).getBlock() && shouldReplaceDeviceFor(state, newState)) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            sd.removeDevice(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected boolean shouldReplaceDeviceFor(BlockState thisState, BlockState newState) {
        return true;
    }
}
