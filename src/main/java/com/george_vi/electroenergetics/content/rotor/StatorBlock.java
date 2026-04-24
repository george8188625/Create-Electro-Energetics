package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatorBlock extends Block implements IWrenchable, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = DirectionalRolledDeviceBlock.ROLL;
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
                .setValue(ROLL, false)
                .setValue(FULL, false));
    }

    public static boolean canPowerRotor(BlockPos pos, BlockState state, BlockPos rotorPos,
                                        BlockState rotorState) {
        Direction facing = state.getValue(FACING);
        Direction.Axis rotorAxis = getRotorAxis(state);

        return pos.relative(facing).equals(rotorPos) &&
                rotorState.getValue(AlternatorRotorBlock.AXIS) == rotorAxis;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROLL, WATERLOGGED, FULL);
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_3PX.get(state.getValue(FACING));
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
            return withWater(defaultBlockState().setValue(FACING, context.getClickedFace())
                    .setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context);
        return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()), context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        Direction facing = state.getValue(FACING);
        BlockPos rotorPos = pos.relative(facing);
        BlockState rotorState = level.getBlockState(rotorPos);

        boolean shouldBeFull = shouldBeFull(state, level, pos);
        if (shouldBeFull != state.getValue(FULL)) {
            level.setBlockAndUpdate(pos, state.setValue(FULL, shouldBeFull));
            level.updateNeighborsAtExceptFromFacing(rotorPos, rotorState.getBlock(), facing.getOpposite());
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    private static boolean shouldBeFull(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction.Axis rotorAxis = getRotorAxis(state);

        BlockPos rotorPos = pos.relative(facing);
        BlockState rotorState = level.getBlockState(rotorPos);

        if (!CEEBlocks.ALTERNATOR_ROTOR.has(rotorState) || rotorState.getValue(AlternatorRotorBlock.AXIS) != rotorAxis)
            return false;

        for (Direction dir : Iterate.directions) {
            if (dir.getAxis() == rotorAxis)
                continue;

            BlockPos otherStatorPos = rotorPos.relative(dir);

            BlockState otherStatorState = level.getBlockState(otherStatorPos);
            if (!CEEBlocks.STATOR.has(otherStatorState) ||
                    !StatorBlock.canPowerRotor(otherStatorPos, otherStatorState, rotorPos, rotorState))
                return false;
        }

        return true;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        return state.setValue(FULL, shouldBeFull(state, level, pos));
    }

    public static Direction.Axis getRotorAxis(BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean roll = state.getValue(ROLL);
        Direction.Axis rotorAxis = facing.getAxis().isHorizontal() ?
                roll ? facing.getClockWise().getAxis() : Direction.Axis.Y :
                roll ? Direction.Axis.X : Direction.Axis.Z;
        return rotorAxis;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
