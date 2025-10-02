package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreativeBatteryBlock extends SimpleDeviceBlock implements IBE<CreativeBatteryBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CreativeBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CREATIVE_BATTERY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof CreativeBatteryBlockEntity be)
            tag.putDouble("Voltage", be.indexToVoltage(be.voltage.value));
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection());
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
    public Class<CreativeBatteryBlockEntity> getBlockEntityClass() {
        return CreativeBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeBatteryBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CREATIVE_BATTERY.get();
    }
}
