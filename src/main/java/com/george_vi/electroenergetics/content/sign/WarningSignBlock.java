package com.george_vi.electroenergetics.content.sign;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.content.pole.ConcretePoleBlock;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WarningSignBlock extends Block implements ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    public WarningSignBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(ATTACHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, ATTACHED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace().getOpposite();
        if (facing.getAxis().isVertical())
            facing = context.getHorizontalDirection().getOpposite();

        BlockState state = withWater(defaultBlockState().setValue(FACING, facing), context);
        BlockState attachedToState = context.getLevel().getBlockState(context.getClickedPos().relative(facing));

        return state.setValue(ATTACHED, !attachedToState.isFaceSturdy(context.getLevel(), context.getClickedPos(), facing.getOpposite()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.WARNING_SIGN.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        if (!canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();

        BlockState attachedToState = level.getBlockState(pos.relative(state.getValue(FACING)));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos).setValue(ATTACHED, !attachedToState.isFaceSturdy(level, pos, state.getValue(FACING).getOpposite()));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState attachedToState = level.getBlockState(pos.relative(state.getValue(FACING)));
        return attachedToState.getBlock() instanceof FenceBlock || attachedToState.getBlock() instanceof GirderBlock ||
                attachedToState.getBlock() instanceof ConcretePoleBlock || attachedToState.getBlock() instanceof WallBlock ||
                attachedToState.getBlock() instanceof IronBarsBlock ||
                attachedToState.isFaceSturdy(level, pos, state.getValue(FACING).getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
