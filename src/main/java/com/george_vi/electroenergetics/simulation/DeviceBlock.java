package com.george_vi.electroenergetics.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public interface DeviceBlock {
    /**
     * This is used get all nodes, their positions and IDs (block-local)
     * @param level level
     * @param pos pos
     * @param state state
     * @return map of positions (relative to the bottom corner of the block) and node IDs (block-local)
     */
    Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state);

    /**
     * This is used to get the position of a specific node
     * @param level level
     * @param pos pos
     * @param state state
     * @param id ID of the node (block-local)
     * @return position of the node (relative to the bottom corner of the block)
     */
    Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id);

    /**
     * This is used to know if these nodes can be connected
     * @param level level
     * @param pos pos
     * @param state state
     * @param id1 ID of the first node (block-local)
     * @param id2 ID of the second node (block-local)
     * @return Whenever these nodes can be connected
     */
    default boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return false;
    }

    /**
     * This is used to get the label of a specific node
     * @param level level
     * @param pos pos
     * @param state state
     * @param id ID of the node (block-local)
     * @return component
     */
    default MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return Component.translatable("electroenergetics.nodes.node");
    }

    /**
     * Should an insulator rendered on the wire connected to a specific node
     * @param level level
     * @param pos pos
     * @param state state
     * @param id ID of the node (block-local)
     * @return Whenever an insulator should be rendered on the wire
     */
    default boolean isOuterInsulator(Level level, BlockPos pos, BlockState state, int id) {
        return false;
    }
}
