package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BulbBlock extends SimpleDeviceBlock implements SimpleWaterloggedBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 2);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final boolean broken;

    public BulbBlock(Properties properties) {
        this(properties, false);
    }

    public BulbBlock(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    public static BulbBlock broken(Properties properties) {
        return new BulbBlock(properties, true);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.BULB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROLL, LIGHT, WATERLOGGED);
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
            return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace()).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X);
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.BULB_ROLL : CEEShapes.BULB).get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = InfrastructureSavedData.load(sl).getDevice(pos);
            if (deviceInstance != null)
                deviceInstance.extraData().putBoolean("Destroyed", broken);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (broken)
            return 0;
        return state.getValue(LIGHT) * 7;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected boolean shouldReplaceDeviceFor(BlockState thisState, BlockState newState) {
        return thisState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
