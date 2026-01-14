package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
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
                return new ValueSettingsBoard(label, max, 10, ImmutableList.of(CEELang.translate("resistor.resistance_symbol").component()),
                        new ValueSettingsFormatter(valueSettings -> CEELang.formatResistance(indexToResistance(valueSettings.value())).component()));
            }
        };
        resistance.between(0, 550);
        resistance.value = 24;
        resistance.withFormatter(v -> CEELang.formatResistance(indexToResistance(v)).string());
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    double indexToResistance(int i) {
        if (i < 100)
            return i / 10d;
        i -= 100;
        if (i < 90)
            return (i + 10);
        i -= 90;
        if (i < 90)
            return (i + 10) * 10d;
        i -= 90;
        if (i < 90)
            return (i + 10) * 100d;
        i -= 90;
        if (i < 90)
            return (i + 10) * 1000d;
        i -= 90;
        return (i + 10) * 10000d;
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        SimulatedDeviceInstance<?> deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null && deviceInstance.extraData() instanceof ResistorDevice.DataHolder dataHolder) {
            dataHolder.properties.resistance = Math.max(0.01, indexToResistance(resistance.value));
        } else if (deviceInstance != null && deviceInstance.extraData() instanceof CreativeResistorDevice.DataHolder dataHolder) {
            dataHolder.properties.resistance = Math.max(0.01, indexToResistance(resistance.value));
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
