package com.george_vi.electroenergetics.content.transmission_distribution.current_transformer;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CurrentTransformerBlock extends SimpleElectricalDeviceBlock<CurrentTransformerDevice> implements ProperWaterloggedBlock, IBE<CurrentTransformerBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public CurrentTransformerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TOP, BOTTOM);
    }


    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof CurrentTransformerBlockEntity be)
            tag.putDouble("Ratio", be.scaling.getScale());
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        Direction facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ?
                context.getHorizontalDirection() : context.getHorizontalDirection().getOpposite();
        return withWater(defaultBlockState().setValue(BOTTOM, !CEEBlocks.CURRENT_TRANSFORMER.has(below))
                .setValue(TOP, !CEEBlocks.CURRENT_TRANSFORMER.has(above))
                .setValue(FACING, facing), context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TOP) ? CEEShapes.CURRENT_TRANSFORMER_TOP.get(Direction.Axis.Y) : AllShapes.TEN_VOXEL_POLE.get(Direction.Axis.Y);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        boolean change = false;
        if (CEEBlocks.CURRENT_TRANSFORMER.has(below) == state.getValue(BOTTOM) ||
                CEEBlocks.CURRENT_TRANSFORMER.has(above) == state.getValue(TOP)) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, this))
                level.scheduleTick(pos, this, 1);
            change = true;
        }


        updateWater(level, state, pos);

        boolean bottom = !CEEBlocks.CURRENT_TRANSFORMER.has(below);
        boolean top = !CEEBlocks.CURRENT_TRANSFORMER.has(above);
        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            CurrentTransformerDevice device = DevicesSavedData.load(sl).getDevice(pos, CurrentTransformerDevice.class);
            if (device != null) {
                device.top = top;
                device.bottom = bottom;
            }

            if (change)
                for (InWorldNodeData nodeData : sd.getNodesAt(pos)) {
                    List<InWorldNodeConnection> connections = sd.getConnections(nodeData);
                    for (InWorldNodeConnection connection : connections)
                        Containers.dropItemStack(sl, pos.getX(), pos.getY(), pos.getZ(),
                                new ItemStack(sd.removeConnection(connection).wireType().getDrops(),
                                        CEEConfigs.server().wiresPerSpool.get()));
                }
        }

        return state.setValue(BOTTOM, bottom)
                .setValue(TOP, top);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        boolean bottom = !CEEBlocks.CURRENT_TRANSFORMER.has(below);
        boolean top = !CEEBlocks.CURRENT_TRANSFORMER.has(above);

        CurrentTransformerDevice device = DevicesSavedData.load(level).getDevice(pos, CurrentTransformerDevice.class);
        if (device != null) {
            device.top = top;
            device.bottom = bottom;
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public SimulatedDeviceType<CurrentTransformerDevice> getDevice() {
        return CEESimulatedDevices.CURRENT_TRANSFORMER.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_BOTH.getNodes(state.getValue(FACING));
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_BOTTOM.getNodes(state.getValue(FACING));
        else if (!state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_TOP.getNodes(state.getValue(FACING));
        return Collections.emptyMap();
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return super.getNodeLabel(level, pos, state, id);
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_BOTH.getNodePos(state.getValue(FACING), id);
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_BOTTOM.getNodePos(state.getValue(FACING), id);
        else if (!state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.CURRENT_TRANSFORMER_TOP.getNodePos(state.getValue(FACING), id);
        return null;
    }

    @Override
    public Class<CurrentTransformerBlockEntity> getBlockEntityClass() {
        return CurrentTransformerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CurrentTransformerBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CURRENT_TRANSFORMER.get();
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
