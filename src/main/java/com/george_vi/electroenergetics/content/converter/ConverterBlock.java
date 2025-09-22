package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
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

public class ConverterBlock extends DirectionalRolledDeviceBlock implements IWrenchable, IBE<ConverterBlockEntity> {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");

    public ConverterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SOURCE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SOURCE);
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Source", state.getValue(SOURCE));
        return tag;
    }


    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONVERTER;
    }


    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace().getAxis() == state.getValue(FACING).getAxis())
            return super.onWrenched(state, context);

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(context.getClickedPos());
            if (device != null)
                device.extraData().putBoolean("Source", !state.getValue(SOURCE));
            serverLevel.setBlockAndUpdate(context.getClickedPos(), state.cycle(SOURCE));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(context.getLevel(), context.getClickedPos());
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(SOURCE, context.getPlayer() != null && context.getPlayer().isShiftKeyDown());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.CONVERTER.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(ROLL))
            return CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodes(state.getValue(FACING));
        return CEENodeConfigurations.DOUBLE_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(ROLL))
            return CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.DOUBLE_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<ConverterBlockEntity> getBlockEntityClass() {
        return ConverterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConverterBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CONVERTER.get();
    }
}
