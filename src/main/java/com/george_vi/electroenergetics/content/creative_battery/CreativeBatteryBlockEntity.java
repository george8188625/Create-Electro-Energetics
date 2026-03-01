package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CreativeBatteryBlockEntity extends SmartBlockEntity {

    public CreativeBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour voltage;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        voltage = new ScrollValueBehaviour(CEELang.translate("creative_battery.voltage").component(), this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, 190, 10, ImmutableList.of(CEELang.translate("creative_battery.voltage_symbol").component(),
                                                                                             CEELang.translate("creative_battery.kilo_voltage_symbol").component()),
                        new ValueSettingsFormatter(valueSettings -> CEELang.formatVoltage(indexToVoltage(valueSettings.value(), valueSettings.row())).component()));
            }

            @Override
            public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
                int value = Math.max(1, valueSetting.value());
                if (!valueSetting.equals(getValueSettings()))
                    playFeedbackSound(this);
                setValue((int) (indexToVoltage(valueSetting.value(), valueSetting.row()) * 1000));
            }

            @Override
            public String formatValue() {
                return CEELang.formatVoltage(value / 1000d).string();
            }
        };
        voltage.between(0, 1_000_000_000);

        voltage.value = 300_000;
        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    private double indexToVoltage(int i, int row) {
        if (row == 0) {

            if (i < 100)
                return i;
            else
                return (i - 90) * 10;
        }

        if (i < 100)
            return i == 0 ? 1000 : i * 1000;
        else
            return (i - 90) * 10000;
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        SimulatedDeviceInstance<?> deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null && deviceInstance.extraData() instanceof CreativeBatteryDevice.DataHolder dataHolder) {
            dataHolder.voltage = voltage.value / 1000d;
        }
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).getAxis().isHorizontal() ? (direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(CreativeBatteryBlock.FACING).getAxis()) : direction.getAxis() == Direction.Axis.X;
        }
    }
}
