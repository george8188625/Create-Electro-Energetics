package com.george_vi.electroenergetics.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public interface DeviceBlock {
    Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state);

    Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id);

    default boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return false;
    }

    default MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return Component.translatable("electroenergetics.nodes.node");
    }

    default boolean isOuterInsulator(Level level, BlockPos blockPos, BlockState state, int id) {
        return false;
    }
}
