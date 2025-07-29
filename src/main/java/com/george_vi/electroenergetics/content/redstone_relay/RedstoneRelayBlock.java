package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RedstoneRelayBlock extends SimpleDeviceBlock implements IWrenchable, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public RedstoneRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(POWERED, false));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.hasNeighborSignal(pos) != state.getValue(POWERED))
            level.setBlockAndUpdate(pos, state.cycle(POWERED));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(pos);

        if (instance == null)
            return;

        instance.extraData().putBoolean("Powered", state.getValue(POWERED));
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.REDSTONE_RELAY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, ROLL, POWERED);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return IWrenchable.super.getRotatedBlockState(originalState, targetedFace);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isVertical())
            return withWater(super.getStateForPlacement(context).setValue(FACING, context.getClickedFace()).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context).setValue(POWERED,
                    context.getLevel().hasNeighborSignal(context.getClickedPos()));
        return withWater(super.getStateForPlacement(context).setValue(FACING, context.getClickedFace()), context).setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.REDSTONE_RELAY.get(state.getValue(FACING));
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
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.REDSTONE_RELAY_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.REDSTONE_RELAY.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.REDSTONE_RELAY_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.REDSTONE_RELAY.getNodePos(state.getValue(FACING), id);
    }

}
