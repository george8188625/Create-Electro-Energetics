package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.AllShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AccumulatorBlock extends SimpleDeviceBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AccumulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_14PX.get(Direction.UP);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.ACCUMULATOR;
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            return Map.of(new Vec3(0.5f, 14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 5 : 11)/16f), 0,
                    new Vec3(0.5f, 14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 5 : 11)/16f), 1);
        return Map.of(new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 5 : 11)/16f, 14/16f, 0.5f), 0,
                new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 5 : 11)/16f, 14/16f, 0.5f), 1);
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            return id == 0 ? new Vec3(0.5f, 14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 5 : 11)/16f) :
                    new Vec3(0.5f, 14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 5 : 11)/16f);
        return id == 0 ? new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 5 : 11)/16f, 14/16f, 0.5f) :
                new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 5 : 11)/16f, 14/16f, 0.5f);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 0 ? Component.translatable("electroenergetics.nodes.negative") : Component.translatable("electroenergetics.nodes.positive");
    }
}
