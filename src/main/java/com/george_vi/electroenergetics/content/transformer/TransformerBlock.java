package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.content.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TransformerBlock extends SimpleDeviceBlock implements SimpleWaterloggedBlock, IBE<TransformerBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TransformerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.TRANSFORMER.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.TRANSFORMER;
    }

    // 0,1 Primary / 2,3 Secondary
    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FACING) == Direction.NORTH)
            return Map.of(
                    new Vec3(5/16f, 14/16f, 13/16f), 0,
                    new Vec3(11/16f, 14/16f, 13/16f), 1,
                    new Vec3(5/16f, 14/16f, 3/16f), 2,
                    new Vec3(11/16f, 14/16f, 3/16f), 3
            );
        if (state.getValue(FACING) == Direction.EAST)
            return Map.of(
                    new Vec3(3/16f, 14/16f, 5/16f), 0,
                    new Vec3(3/16f, 14/16f, 11/16f), 1,
                    new Vec3(13/16f, 14/16f, 5/16f), 2,
                    new Vec3(13/16f, 14/16f, 11/16f), 3
            );
        if (state.getValue(FACING) == Direction.SOUTH)
            return Map.of(
                    new Vec3(5/16f, 14/16f, 3/16f), 0,
                    new Vec3(11/16f, 14/16f, 3/16f), 1,
                    new Vec3(5/16f, 14/16f, 13/16f), 2,
                    new Vec3(11/16f, 14/16f, 13/16f), 3
            );
        return Map.of(
                new Vec3(13/16f, 14/16f, 5/16f), 0,
                new Vec3(13/16f, 14/16f, 11/16f), 1,
                new Vec3(3/16f, 14/16f, 5/16f), 2,
                new Vec3(3/16f, 14/16f, 11/16f), 3
        );
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 0 || id == 1 ? Component.translatable("electroenergetics.nodes.primary") : Component.translatable("electroenergetics.nodes.secondary");
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(FACING) == Direction.NORTH)
            return id == 0 ? new Vec3(5/16f, 14/16f, 13/16f) :
                    id == 1 ? new Vec3(11/16f, 14/16f, 13/16f) :
                    id == 2 ? new Vec3(5/16f, 14/16f, 3/16f) :
                    new Vec3(11/16f, 14/16f, 3/16f);
        if (state.getValue(FACING) == Direction.EAST)
            return id == 0 ? new Vec3(3/16f, 14/16f, 5/16f) :
                    id == 1 ? new Vec3(3/16f, 14/16f, 11/16f) :
                    id == 2 ? new Vec3(13/16f, 14/16f, 5/16f) :
                    new Vec3(13/16f, 14/16f, 11/16f);
        if (state.getValue(FACING) == Direction.SOUTH)
            return id == 0 ? new Vec3(5/16f, 14/16f, 3/16f) :
                    id == 1 ? new Vec3(11/16f, 14/16f, 3/16f) :
                    id == 2 ? new Vec3(5/16f, 14/16f, 13/16f) :
                    new Vec3(11/16f, 14/16f, 13/16f);
        return id == 0 ? new Vec3(13/16f, 14/16f, 5/16f) :
                id == 1 ? new Vec3(13/16f, 14/16f, 11/16f) :
                id == 2 ? new Vec3(3/16f, 14/16f, 5/16f) :
                new Vec3(3/16f, 14/16f, 11/16f);
    }

    @Override
    protected CompoundTag getExtraData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("ratio", 3);
        return tag;
    }

    @Override
    public Class<TransformerBlockEntity> getBlockEntityClass() {
        return TransformerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TransformerBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.TRANSFORMER.get();
    }
}
