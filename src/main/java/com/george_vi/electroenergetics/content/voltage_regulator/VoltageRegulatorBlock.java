package com.george_vi.electroenergetics.content.voltage_regulator;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.SimpleDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
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

public class VoltageRegulatorBlock extends SimpleDeviceBlock implements ProperWaterloggedBlock, IBE<VoltageRegulatorBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public VoltageRegulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TOP, BOTTOM);
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
            tag.putDouble("Voltage", be.voltage.getVoltage());
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        return withWater(defaultBlockState().setValue(BOTTOM, !CEEBlocks.CONCRETE_POLE.has(below))
                .setValue(TOP, !CEEBlocks.VOLTAGE_REGULATOR.has(above)).setValue(FACING, CEEBlocks.VOLTAGE_REGULATOR.has(below) ? below.getValue(FACING) : CEEBlocks.VOLTAGE_REGULATOR.has(above) ? above.getValue(FACING) : context.getHorizontalDirection()), context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.VOLTAGE_REGULATOR.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        boolean change = false;
        if (CEEBlocks.VOLTAGE_REGULATOR.has(below) == state.getValue(BOTTOM) ||
                CEEBlocks.VOLTAGE_REGULATOR.has(above) == state.getValue(TOP)) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, this))
                level.scheduleTick(pos, this, 1);
            change = true;
        }

        updateWater(level, state, pos);

        boolean bottom = !CEEBlocks.VOLTAGE_REGULATOR.has(below);
        boolean top = !CEEBlocks.VOLTAGE_REGULATOR.has(above);
        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            SimulatedDeviceInstance<?> device = sd.getDevice(pos);

            if (device != null && device.extraData() instanceof VoltageRegulatorDevice.DataHolder dataHolder) {
                dataHolder.top = top;
                dataHolder.bottom = bottom;
            }

            if (change)
                for (InWorldNode node : sd.getNodesAt(pos)) {
                    List<InWorldNodeConnection> connections = sd.getConnections(node);
                    for (InWorldNodeConnection connection : connections)
                        Containers.dropItemStack(sl, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(sd.removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
                }
        }

        if (!bottom && direction == Direction.DOWN &&
                below.getValue(FACING) != state.getValue(FACING))
            level.setBlock(pos, state.setValue(FACING, below.getValue(FACING)), 3);
        if (!top && direction == Direction.UP &&
                above.getValue(FACING) != state.getValue(FACING))
            level.setBlock(pos, state.setValue(FACING, above.getValue(FACING)), 3);

        return state.setValue(BOTTOM, bottom)
                .setValue(TOP, top);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        boolean bottom = !CEEBlocks.VOLTAGE_REGULATOR.has(below);
        boolean top = !CEEBlocks.VOLTAGE_REGULATOR.has(above);

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        SimulatedDeviceInstance<?> device = sd.getDevice(pos);
        if (device != null && device.extraData() instanceof VoltageRegulatorDevice.DataHolder dataHolder) {
            dataHolder.top = top;
            dataHolder.bottom = bottom;
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.VOLTAGE_REGULATOR;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTH.getNodes(state.getValue(FACING));
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodes(state.getValue(FACING));
        else if (!state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_TOP.getNodes(state.getValue(FACING));
        return Collections.emptyMap();
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(TOP))
            return id == 0 ? CEELang.nodeLabel("input") : id == 1 ? CEELang.nodeLabel("output") : CEELang.nodeLabel("neutral");
        return CEELang.nodeLabel("neutral");
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTH.getNodePos(state.getValue(FACING), id);
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodePos(state.getValue(FACING), id);
        else if (!state.getValue(BOTTOM) && state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_TOP.getNodePos(state.getValue(FACING), id);
        return null;
    }

    @Override
    public Class<VoltageRegulatorBlockEntity> getBlockEntityClass() {
        return VoltageRegulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends VoltageRegulatorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.VOLTAGE_REGULATOR.get();
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
