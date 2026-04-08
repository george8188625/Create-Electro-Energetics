package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TransformerCoreBlock extends SimpleElectricalDeviceBlock<TransformerCoreDevice> implements ProperWaterloggedBlock, IBE<TransformerCoreBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TransformerCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().relative(context.getHorizontalDirection())).canBeReplaced(context))
            return withWater(defaultBlockState().setValue(FACING, context.getHorizontalDirection()), context);
        return null;
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof TransformerCoreBlockEntity be) {
            Direction facing = state.getValue(TransformerCoreBlock.FACING);
            BlockPos otherPos = pos.relative(facing);
            if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be2) {
                    tag.putDouble("Ratio", (double) be.turns.value / be2.turns.value);
                }
            }
        }
        return tag;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        Direction facing = state.getValue(FACING);
        level.setBlockAndUpdate(pos.relative(facing), defaultBlockState().setValue(FACING, facing.getOpposite()).setValue(WATERLOGGED, level.getFluidState(pos.relative(facing)).is(Tags.Fluids.WATER)));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        Direction facing = state.getValue(FACING);
        if (direction != facing)
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (!CEEBlocks.TRANSFORMER_CORE.has(neighborState) || neighborState.getValue(FACING) != facing.getOpposite())
            return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        Direction facing = state.getValue(FACING);
        if (!level.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state, level, pos)) && (facing == Direction.SOUTH || facing == Direction.WEST)) {
            BlockPos otherPos = pos.relative(facing);
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(state.getBlock()) && otherState.getValue(FACING) == facing.getOpposite()) {
                BlockState newState = otherState.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(otherPos, newState, 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }

        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        TransformerCoreDevice device = DevicesSavedData.load(level).getDevice(pos, TransformerCoreDevice.class);
        if (device != null)
            device.facing = state.getValue(FACING);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public SimulatedDeviceType<TransformerCoreDevice> getDevice() {
        return CEESimulatedDevices.TRANSFORMER_CORE.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.TRANSFORMER_CORE.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.TRANSFORMER_CORE.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.TRANSFORMER_CORE.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<TransformerCoreBlockEntity> getBlockEntityClass() {
        return TransformerCoreBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TransformerCoreBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.TRANSFORMER_CORE.get();
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
