package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreativeBatteryBlock extends SimpleElectricalDeviceBlock<CreativeBatteryDevice> implements IBE<CreativeBatteryBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty AC = BooleanProperty.create("ac");

    public CreativeBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AC, false));
    }

    @Override
    public SimulatedDeviceType<CreativeBatteryDevice> getDevice() {
        return CEESimulatedDevices.CREATIVE_BATTERY.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AC);
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof CreativeBatteryBlockEntity be)
            tag.putDouble("Voltage", be.voltage.getVoltage());
        if (state.getValue(AC))
            tag.putDouble("ACFrequency", 20);
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        CreativeBatteryDevice device = DevicesSavedData.load(level).getDevice(pos, CreativeBatteryDevice.class);
        if (device != null)
            device.acFrequency = state.getValue(AC) ? 20 : 0;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 0 ? Component.translatable("electroenergetics.nodes.negative") : Component.translatable("electroenergetics.nodes.positive");
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(AC);
    }

    @Override
    public Class<CreativeBatteryBlockEntity> getBlockEntityClass() {
        return CreativeBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeBatteryBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CREATIVE_BATTERY.get();
    }
}
