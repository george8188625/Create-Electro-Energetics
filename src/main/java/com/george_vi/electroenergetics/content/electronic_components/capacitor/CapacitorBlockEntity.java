package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CapacitorBlockEntity extends SmartBlockEntity {
    public CapacitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour capacitance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        capacitance = new ScrollValueBehaviour(CEELang.translate("capacitor.capacitance").component(), this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, 10, ImmutableList.of(CEELang.translate("capacitor.capacitance_symbol").component()),
                        new ValueSettingsFormatter(valueSettings -> CEELang.formatCapacitance(indexToCapacitance(valueSettings.value())).component()));
            }
        };
        capacitance.between(0, 190);
        capacitance.value = 1;
        capacitance.withFormatter(v -> CEELang.formatCapacitance(indexToCapacitance(v)).string());
        capacitance.withCallback(i -> this.updateCapacitance());
        behaviours.add(capacitance);
    }

    private void updateCapacitance() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null) {
            deviceInstance.extraData().putDouble("Capacitance", indexToCapacitance(capacitance.value));
        }
    }

    double indexToCapacitance(int i) {
        if (i < 100)
            return i / 100000d;
        i -= 100;
        return (i + 10) / 10000d;
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 10);
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
