package com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class HVCapacitorBlock extends DirectionalRolledDeviceBlock<HVCapacitorDevice> implements IBE<HVCapacitorBlockEntity> {
    public static final BooleanProperty SLICED = BooleanProperty.create("sliced");

    public HVCapacitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SLICED, false));
    }

    @Override
    public SimulatedDeviceType<HVCapacitorDevice> getDevice() {
        return CEESimulatedDevices.HV_CAPACITOR.get();
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof HVCapacitorBlockEntity be)
            tag.putDouble("Capacitance", be.capacitance.getCapacitance());
        if (state.getValue(SLICED))
            tag.putString("Facing", state.getValue(FACING).getSerializedName());
        return tag;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        BlockState aboveState = context.getLevel().getBlockState(context.getClickedPos()
                .relative(state.getValue(FACING)));
        boolean sliced = CEEBlocks.DOUBLE_CONNECTOR.has(aboveState) &&
                aboveState.getValue(FACING) == state.getValue(FACING) &&
                aboveState.getValue(ROLL) == state.getValue(ROLL);
        return state.setValue(SLICED, sliced);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SLICED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        HVCapacitorDevice device = DevicesSavedData.load(level).getDevice(pos, HVCapacitorDevice.class);
        if (device != null)
            device.facing = state.getValue(SLICED) ? state.getValue(FACING) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState aboveState = level.getBlockState(pos.relative(state.getValue(FACING)));
        boolean sliced = CEEBlocks.DOUBLE_CONNECTOR.has(aboveState) &&
                aboveState.getValue(FACING) == state.getValue(FACING) &&
                aboveState.getValue(ROLL) == state.getValue(ROLL);
        if (sliced != state.getValue(SLICED)) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, this))
                level.scheduleTick(pos, this, 1);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos).setValue(SLICED, sliced);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return state.getValue(ROLL) ?
                CEEShapes.HV_CAPACITOR_ROLL.get(state.getValue(FACING)) :
                CEEShapes.HV_CAPACITOR.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(SLICED))
            return Collections.emptyMap();
        return state.getValue(ROLL) ?
                CEENodeConfigurations.HV_CAPACITOR.rotate(new Vec3(0, 90, 0))
                        .getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.HV_CAPACITOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(SLICED))
            return null;
        return state.getValue(ROLL) ?
                CEENodeConfigurations.HV_CAPACITOR.rotate(new Vec3(0, 90, 0))
                        .getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.HV_CAPACITOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == Direction.Axis.Z)
            return super.getRotatedBlockState(originalState, targetedFace).cycle(ROLL);

        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Override
    public Class<HVCapacitorBlockEntity> getBlockEntityClass() {
        return HVCapacitorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HVCapacitorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.HV_CAPACITOR.get();
    }
}
