package com.george_vi.electroenergetics.foundation;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public abstract class DirectionalRolledDeviceBlock extends SimpleDeviceBlock implements ProperWaterloggedBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DirectionalRolledDeviceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(ROLL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROLL, WATERLOGGED);
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
            return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context);
        return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()), context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }


    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
                return state.cycle(ROLL);
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
