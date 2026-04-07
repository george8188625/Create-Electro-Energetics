package com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorBlock;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
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

public class VoltageRegulatorBlock extends SimpleElectricalDeviceBlock<VoltageRegulatorDevice> implements ProperWaterloggedBlock, IBE<VoltageRegulatorBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty SLICED = BooleanProperty.create("sliced");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public VoltageRegulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(SLICED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TOP, BOTTOM, SLICED);
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
            tag.putDouble("Voltage", be.voltage.getVoltage());
        if (state.getValue(SLICED))
            tag.putInt("Sliced", state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 2 : 1);
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        Direction facing = CEEBlocks.VOLTAGE_REGULATOR.has(below) ? below.getValue(FACING) :
                CEEBlocks.VOLTAGE_REGULATOR.has(above) ? above.getValue(FACING) :
                    context.getHorizontalDirection();
        boolean sliced = isValidDoubleConnector(above, facing);
        return withWater(defaultBlockState().setValue(BOTTOM, !CEEBlocks.VOLTAGE_REGULATOR.has(below))
                .setValue(TOP, !CEEBlocks.VOLTAGE_REGULATOR.has(above))
                .setValue(FACING, facing).setValue(SLICED, sliced), context);
    }

    private static boolean isValidDoubleConnector(BlockState above, Direction facing) {
        return CEEBlocks.DOUBLE_CONNECTOR.has(above) &&
                above.getValue(DoubleConnectorBlock.FACING) == Direction.UP &&
                above.getValue(DoubleConnectorBlock.ROLL) == (facing.getAxis() == Direction.Axis.X);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.VOLTAGE_REGULATOR.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
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

        boolean sliced = isValidDoubleConnector(above, state.getValue(FACING));

        updateWater(level, state, pos);

        boolean bottom = !CEEBlocks.VOLTAGE_REGULATOR.has(below);
        boolean top = !CEEBlocks.VOLTAGE_REGULATOR.has(above);
        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            VoltageRegulatorDevice device = DevicesSavedData.load(sl).getDevice(pos, VoltageRegulatorDevice.class);
            if (device != null) {
                device.top = top;
                device.bottom = bottom;
            }

            if (change || sliced != state.getValue(SLICED))
                for (InWorldNode node : sd.getNodesAt(pos)) {
                    List<InWorldNodeConnection> connections = sd.getConnections(node);
                    for (InWorldNodeConnection connection : connections)
                        Containers.dropItemStack(sl, pos.getX(), pos.getY(), pos.getZ(),
                                new ItemStack(sd.removeConnection(connection).wireType().getDrops(),
                                        CEEConfigs.server().wiresPerSpool.get()));
                }
        }


        if (!bottom && direction == Direction.DOWN &&
                below.getValue(FACING) != state.getValue(FACING))
            level.setBlock(pos, state.setValue(FACING, below.getValue(FACING)), 3);
        if (!top && direction == Direction.UP &&
                above.getValue(FACING) != state.getValue(FACING))
            level.setBlock(pos, state.setValue(FACING, above.getValue(FACING)), 3);

        return state.setValue(BOTTOM, bottom)
                .setValue(TOP, top)
                .setValue(SLICED, sliced);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        boolean bottom = !CEEBlocks.VOLTAGE_REGULATOR.has(below);
        boolean top = !CEEBlocks.VOLTAGE_REGULATOR.has(above);

        VoltageRegulatorDevice device = DevicesSavedData.load(level).getDevice(pos, VoltageRegulatorDevice.class);
        if (device != null) {
            device.top = top;
            device.bottom = bottom;
            device.sliced = state.getValue(SLICED) ? state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 2 : 1 : 0;
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public SimulatedDeviceType<VoltageRegulatorDevice> getDevice() {
        return CEESimulatedDevices.VOLTAGE_REGULATOR.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return state.getValue(SLICED) ?
                    CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodes(state.getValue(FACING)) :
                    CEENodeConfigurations.VOLTAGE_REGULATOR_BOTH.getNodes(state.getValue(FACING));
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodes(state.getValue(FACING));
        else if (!state.getValue(BOTTOM) && state.getValue(TOP) && !state.getValue(SLICED))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_TOP.getNodes(state.getValue(FACING));
        return Collections.emptyMap();
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(TOP) && !state.getValue(SLICED))
            return id == 0 ? CEELang.nodeLabel("input") :
                    id == 1 ? CEELang.nodeLabel("output") :
                    CEELang.nodeLabel("ground");
        return CEELang.nodeLabel("ground");
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(BOTTOM) && state.getValue(TOP))
            return state.getValue(SLICED) ?
                    CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodePos(state.getValue(FACING), id) :
                    CEENodeConfigurations.VOLTAGE_REGULATOR_BOTH.getNodePos(state.getValue(FACING), id);
        else if (state.getValue(BOTTOM) && !state.getValue(TOP))
            return CEENodeConfigurations.VOLTAGE_REGULATOR_BOTTOM.getNodePos(state.getValue(FACING), id);
        else if (!state.getValue(BOTTOM) && state.getValue(TOP) && !state.getValue(SLICED))
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
