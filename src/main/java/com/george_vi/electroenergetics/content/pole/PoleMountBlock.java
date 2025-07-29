package com.george_vi.electroenergetics.content.pole;

import com.george_vi.electroenergetics.CEEShapes;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class PoleMountBlock extends Block implements IWrenchable, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PoleMountBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(INVERTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INVERTED, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedOn = context.getClickedFace();
        if (clickedOn.getAxis().isVertical())
            return null;
        return withWater(defaultBlockState().setValue(FACING, clickedOn.getOpposite()), context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(INVERTED))
            return CEEShapes.POLE_MOUNT_INVERTED.get(state.getValue(FACING));
        return CEEShapes.POLE_MOUNT.get(state.getValue(FACING));
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(INVERTED);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState attachedToState = level.getBlockState(pos.relative(state.getValue(FACING)));
        return attachedToState.getBlock() instanceof FenceBlock || attachedToState.getBlock() instanceof GirderBlock ||
                attachedToState.getBlock() instanceof ConcretePoleBlock || attachedToState.getBlock() instanceof WallBlock ||
                attachedToState.isFaceSturdy(level, pos, state.getValue(FACING).getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
                                boolean moved) {
        if (level.isClientSide)
            return;

        if (!canSurvive(state, level, pos))
            level.destroyBlock(pos, true);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
