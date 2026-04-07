package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.VoltageScrollValueBehaviour;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CreativeBatteryBlockEntity extends SmartBlockEntity {

    public CreativeBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected VoltageScrollValueBehaviour voltage;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        voltage = new VoltageScrollValueBehaviour(CEELang.translateDirect("creative_battery.voltage"),
                this, new ValueBox());
        voltage.withCallback(i -> this.updateVoltage());

        behaviours.add(voltage);
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        CreativeBatteryDevice device = DevicesSavedData.load(sl).getDevice(getBlockPos(), CreativeBatteryDevice.class);

        if (device != null)
            device.voltage = voltage.getVoltage();
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
            return state.getValue(CreativeBatteryBlock.FACING).getAxis().isHorizontal() ?
                    (direction.getAxis().isHorizontal() &&
                            direction.getAxis() != state.getValue(CreativeBatteryBlock.FACING).getAxis()) :
                    direction.getAxis() == Direction.Axis.X;
        }
    }
}
