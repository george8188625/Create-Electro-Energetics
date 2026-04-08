package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ResistanceScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ResistorBlockEntity extends SmartBlockEntity {
    public ResistorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ResistanceScrollValueBehaviour resistance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        resistance = new ResistanceScrollValueBehaviour(CEELang.translate("resistor.resistance").component(), this, new ValueBox());
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    public void setResistance(double res) {
        resistance.setValue(Mth.floor(res * 1000));
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;

        ResistorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, ResistorDevice.class);
        if (device != null)
            device.properties.resistance = resistance.getResistance();

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
