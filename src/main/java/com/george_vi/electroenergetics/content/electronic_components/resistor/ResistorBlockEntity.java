package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
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

public class ResistorBlockEntity extends SmartBlockEntity {
    public ResistorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour resistance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        resistance = new ScrollValueBehaviour(CEELang.translate("resistor.resistance").component(), this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, 190, 10, ImmutableList.of(CEELang.translate("resistor.milli_resistance_symbol").component(),
                                                                                                      CEELang.translate("resistor.resistance_symbol").component(),
                                                                                                      CEELang.translate("resistor.kilo_resistance_symbol").component()),
                        new ValueSettingsFormatter(valueSettings ->
                                CEELang.formatResistance(indexToResistance(valueSettings.value(), valueSettings.row())).component()));
            }

            @Override
            public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
                int value = Math.max(1, valueSetting.value());
                if (!valueSetting.equals(getValueSettings()))
                    playFeedbackSound(this);
                setValue((int) (indexToResistance(value, valueSetting.row()) * 1000d));
            }

            @Override
            public String formatValue() {
                return CEELang.formatResistance(value / 1000d).string();
            }
        };
        resistance.between(0, 1_000_000_000);
        resistance.value = 1_000_000;
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    private double indexToResistance(int i, int row) {
        if (row == 0) {
                return (i / 20d) + 0.1;
        } else if (row == 1) {

            if (i < 100)
                return Math.max(1, i);
            else
                return (i - 90) * 10;
        }

        if (i < 100)
            return (i + 10) * 100;
        else
            return (i - 90) * 10000;
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        SimulatedDeviceInstance<?> deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null && deviceInstance.extraData() instanceof ResistorDevice.DataHolder dataHolder) {
            dataHolder.properties.resistance = Math.max(0.01, resistance.value / 1000d);
        } else if (deviceInstance != null && deviceInstance.extraData() instanceof CreativeResistorDevice.DataHolder dataHolder) {
            dataHolder.properties.resistance = Math.max(0.01, resistance.value / 1000d);
        }
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 4);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).equals(direction);
        }
    }
}
