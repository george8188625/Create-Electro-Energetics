package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlockEntity;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorBlockEntity;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConverterBlockEntity extends SmartBlockEntity {
    public ConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour voltage;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        voltage = new ScrollValueBehaviour(Component.translatable("electroenergetics.voltage"), this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, 10, ImmutableList.of(Component.literal("Value")),
                        new ValueSettingsFormatter(valueSettings -> CEELang.formatVoltage(valueSettings.value() * 10).component()));
            }
        };
        voltage.between(0, 432);
        voltage.value = 10;
        voltage.withFormatter(v -> CEELang.formatVoltage(v * 10).string());
        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null) {
            deviceInstance.extraData().putFloat("Voltage", voltage.value * 10);
        }
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 5);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis() == state.getValue(ConverterBlock.FACING).getAxis() && state.getValue(ConverterBlock.SOURCE);
        }
    }
}
