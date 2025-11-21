package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DoubleConnectorBlock extends DirectionalRolledDeviceBlock implements IWrenchable {
    public DoubleConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONNECTOR;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.DOUBLE_CONNECTOR_ROLL : CEEShapes.DOUBLE_CONNECTOR).get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
       return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return true;
    }
}
